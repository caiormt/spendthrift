package spendthrift.application.modules

import cats.implicits.*

import cats.effect.*

import spendthrift.application.modules.controllers.*
import spendthrift.application.modules.usecases.*

object Controllers:
  def make[F[_]: Sync](usecases: UseCases[F]): F[Controllers[F]] = {
    import usecases.*

    for {
      transactionController <- TransactionController.make[F](transactionUseCase)
    } yield new Controllers[F](
      transactionController
    )
  }

final class Controllers[F[_]] private (
    val transactionController: TransactionController[F]
)
