package spendthrift.application.modules.controllers

import cats.implicits.*

import cats.effect.*

import spendthrift.application.modules.*
import spendthrift.application.modules.usecases.*

import spendthrift.presentation.controllers.transaction.*

object TransactionController:
  def make[F[_]: Sync](usecases: TransactionUseCase[F]): F[TransactionController[F]] = {
    import usecases.*

    for {
      registerTransactionController <- Sync[F].delay(new RegisterTransactionController[F](registerTransactionUseCase))
      findTransactionByIdController <- Sync[F].delay(new FindTransactionByIdController[F](findTransactionByIdUseCase))
    } yield new TransactionController[F](
      registerTransactionController,
      findTransactionByIdController
    )
  }

final class TransactionController[F[_]] private (
    val registerTransactionController: RegisterTransactionController[F],
    val findTransactionByIdController: FindTransactionByIdController[F]
)
