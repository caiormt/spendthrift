package spendthrift.application.modules

import cats.implicits.*

import cats.effect.{ Trace => _, * }
import cats.effect.std.*

import fs2.io.net.*

import io.chrisdavenport.epimetheus.*
import io.chrisdavenport.mules.*

import natchez.*

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

  def make[F[_]: Async: Network: Console: Trace](config: AppConfig): Resource[F, Resources[F]] = {
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

    def makeEpimetheus: Resource[F, CollectorRegistry[F]] =
      Resource.eval(CollectorRegistry.buildWithDefaults[F])

    for {
      sessionPool <- makeDatabase(config.database)
      caches      <- makeCaches
      epimetheus  <- makeEpimetheus
    } yield new Resources[F](
      sessionPool,
      caches,
      epimetheus
    )
  }

end Resources

final class Resources[F[_]] private (
    val sessionPool: Resource[F, Session[F]],
    val cache: Resources.CacheResources[F],
    val collectorRegistry: CollectorRegistry[F]
) {

  given CollectorRegistry[F] = collectorRegistry
}
