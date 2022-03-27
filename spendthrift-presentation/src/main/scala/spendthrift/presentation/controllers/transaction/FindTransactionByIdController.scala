package spendthrift.presentation.controllers.transaction

import cats.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.presentation.views.findtransactionsbyid.*
import spendthrift.presentation.views.transactions.*

import spendthrift.queries.usecases.transaction.*

object FindTransactionByIdController:

  def make[F[_]: Sync: Trace](usecase: FindTransactionByIdUseCase[F]): F[FindTransactionByIdController[F]] =
    Sync[F].delay(new FindTransactionByIdController[F](usecase))

end FindTransactionByIdController

final class FindTransactionByIdController[F[_]: Monad: Trace](usecase: FindTransactionByIdUseCase[F]):

  def run(view: FindTransactionById): F[Option[Transaction]] =
    Trace[F].span("controller.find-transaction-by-id") {
      for {
        query       <- Applicative[F].pure(view.toQuery)
        transaction <- usecase.run(query)
      } yield transaction.map(_.toView)
    }
