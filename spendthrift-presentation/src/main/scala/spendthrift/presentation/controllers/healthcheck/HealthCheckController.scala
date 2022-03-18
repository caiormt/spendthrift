package spendthrift.presentation.controllers.healthcheck

import cats.*
import cats.implicits.*

import cats.effect.*

import sup.*
import sup.data.*

object HealthCheckController:

  def make[F[_]: Sync, G[_], H[_]](reporter: HealthReporter[F, G, H]): F[HealthCheckController[F, G, H]] =
    Sync[F].delay(new HealthCheckController[F, G, H](reporter))

end HealthCheckController

final class HealthCheckController[F[_]: Functor, G[_], H[_]](reporter: HealthReporter[F, G, H]):

  def run: F[Report[G, H, Health]] =
    reporter.check.map(_.value)
