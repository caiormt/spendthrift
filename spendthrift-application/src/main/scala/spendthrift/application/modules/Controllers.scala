package spendthrift.application.modules

import cats.data.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import sup.*
import sup.data.*

import spendthrift.application.modules.controllers.*
import spendthrift.application.modules.usecases.*

import spendthrift.presentation.controllers.healthcheck.*

object Controllers:

  def make[F[_]: Sync: Trace](
      usecases: UseCases[F],
      reporter: HealthReporter[F, NonEmptyList, Tagged[String, *]]
  ): F[Controllers[F]] = {
    import usecases.*

    for {
      healthCheckController <- HealthCheckController.make[F, NonEmptyList, Tagged[String, *]](reporter)
      transactionController <- TransactionController.make[F](transactionUseCase)
    } yield new Controllers[F](
      healthCheckController,
      transactionController
    )
  }

end Controllers

final class Controllers[F[_]] private (
    val healthCheckController: HealthCheckController[F, NonEmptyList, Tagged[String, *]],
    val transactionController: TransactionController[F]
)
