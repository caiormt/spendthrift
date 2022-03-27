package spendthrift.application.modules.controllers

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.application.modules.*
import spendthrift.application.modules.usecases.*

import spendthrift.presentation.controllers.transaction.*

object TransactionController:

  def make[F[_]: Sync: Trace](usecases: TransactionUseCase[F]): F[TransactionController[F]] = {
    import usecases.*

    for {
      registerTransactionController <- RegisterTransactionController.make[F](registerTransactionUseCase)
      findTransactionByIdController <- FindTransactionByIdController.make[F](findTransactionByIdUseCase)
    } yield new TransactionController[F](
      registerTransactionController,
      findTransactionByIdController
    )
  }

end TransactionController

final class TransactionController[F[_]] private (
    val registerTransactionController: RegisterTransactionController[F],
    val findTransactionByIdController: FindTransactionByIdController[F]
)
