package spendthrift.presentation.controllers.transaction

import cats.implicits.*

import cats.effect.*

import spendthrift.presentation.views.findtransactionsbyid.*
import spendthrift.presentation.views.transactions.*

import spendthrift.queries.usecases.transaction.*

final class FindTransactionByIdController[F[_]: Sync](usecase: FindTransactionByIdUseCase[F]):

  def run(view: FindTransactionById): F[Option[Transaction]] =
    for {
      query       <- Sync[F].delay(view.toQuery)
      transaction <- usecase.run(query)
    } yield transaction.map(_.toView)
