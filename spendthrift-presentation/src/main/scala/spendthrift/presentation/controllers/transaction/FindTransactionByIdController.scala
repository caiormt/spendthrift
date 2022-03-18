package spendthrift.presentation.controllers.transaction

import cats.*
import cats.implicits.*

import cats.effect.*

import spendthrift.presentation.views.findtransactionsbyid.*
import spendthrift.presentation.views.transactions.*

import spendthrift.queries.usecases.transaction.*

object FindTransactionByIdController:

  def make[F[_]: Sync](usecase: FindTransactionByIdUseCase[F]): F[FindTransactionByIdController[F]] =
    Sync[F].delay(new FindTransactionByIdController[F](usecase))

end FindTransactionByIdController

final class FindTransactionByIdController[F[_]: Monad](usecase: FindTransactionByIdUseCase[F]):

  def run(view: FindTransactionById): F[Option[Transaction]] =
    for {
      query       <- Applicative[F].pure(view.toQuery)
      transaction <- usecase.run(query)
    } yield transaction.map(_.toView)
