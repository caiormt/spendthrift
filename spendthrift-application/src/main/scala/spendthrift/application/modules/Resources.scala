package spendthrift.application.modules

import cats.implicits.*

import cats.effect.*
import cats.effect.std.*

import fs2.io.net.*

import io.chrisdavenport.mules.*

import natchez.Trace.Implicits.noop

import skunk.*
import skunk.implicits.*
import skunk.util.*

import sup.*
import sup.data.*

import spendthrift.application.config.data.*

import scala.concurrent.duration.*

object Resources:
  final class CacheResources[F[_]] private[Resources] (
      val healthCheck: MemoryCache[F, String, HealthResult[Tagged[String, *]]]
  )

  def make[F[_]: Async: Network: Console](config: AppConfig): Resource[F, Resources[F]] = {
    def makeDatabase(config: DatabaseConfig): SessionPool[F] =
      Session.pooled(
        host = config.host,
        user = config.username,
        database = config.database,
        password = config.password.map(_.value),
        max = config.max
      )

    def makeCaches: Resource[F, CacheResources[F]] =
      for {
        healthCheck <- Resource.eval {
                         MemoryCache.ofSingleImmutableMap[F, String, HealthResult[Tagged[String, *]]](
                           TimeSpec.fromDuration(5.minutes)
                         )
                       }
      } yield new CacheResources[F](
        healthCheck
      )

    for {
      sessionPool <- makeDatabase(config.database)
      caches      <- makeCaches
    } yield new Resources[F](
      sessionPool,
      caches
    )
  }

end Resources

final class Resources[F[_]] private (
    val sessionPool: Resource[F, Session[F]],
    val cache: Resources.CacheResources[F]
)
