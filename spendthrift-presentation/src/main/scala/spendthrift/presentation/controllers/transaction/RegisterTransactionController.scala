package spendthrift.presentation.controllers.transaction

import cats.*
import cats.implicits.*

import cats.effect.*

import spendthrift.commands.usecases.transaction.*

import spendthrift.presentation.views.registertransactions.*
import spendthrift.presentation.views.transactions.*

object RegisterTransactionController:

  def make[F[_]: Sync](usecase: RegisterTransactionUseCase[F]): F[RegisterTransactionController[F]] =
    Sync[F].delay(new RegisterTransactionController[F](usecase))

end RegisterTransactionController

final class RegisterTransactionController[F[_]: Monad](usecase: RegisterTransactionUseCase[F]):

  def run(view: RegisterTransaction): F[Transaction] =
    for {
      command     <- Applicative[F].pure(view.toCommand)
      transaction <- usecase.run(command)
    } yield transaction.toView
