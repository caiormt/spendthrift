package spendthrift.presentation.controllers.transaction

import cats.implicits.*

import cats.effect.*

import spendthrift.commands.usecases.transaction.*

import spendthrift.presentation.views.registertransactions.*
import spendthrift.presentation.views.transactions.*

final class RegisterTransactionController[F[_]: Sync](usecase: RegisterTransactionUseCase[F]):

  def run(view: RegisterTransaction): F[Transaction] =
    for {
      command     <- Sync[F].delay(view.toCommand)
      transaction <- usecase.run(command)
    } yield transaction.toView
