package spendthrift.adapters.repositories.sql

import cats.*
import cats.implicits.*

import cats.effect.*

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

import sup.*
import sup.data.*
import sup.mods.*

import scala.concurrent.duration.*

object SkunkHealthCheck:

  def make[F[_]: Async](sessionPool: Resource[F, Session[F]]): F[SkunkHealthCheck[F]] =
    Sync[F].delay(new SkunkHealthCheck[F](sessionPool))

end SkunkHealthCheck

final class SkunkHealthCheck[F[_]: Temporal](sessionPool: Resource[F, Session[F]]):

  private val HEALTHCHECK_QUERY: Query[Void, Int] =
    sql"SELECT 1".query(int4)

  def healthcheck: HealthCheck[F, Tagged[String, *]] =
    HealthCheck
      .liftFBoolean[F] {
        sessionPool.use(_.unique(HEALTHCHECK_QUERY).as(true))
      }
      .through(timeoutToFailure(1.second))
      .through(recoverToSick)
      .through(tagWith("repository"))
