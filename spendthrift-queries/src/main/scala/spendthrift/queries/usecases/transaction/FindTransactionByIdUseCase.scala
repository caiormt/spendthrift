package spendthrift.queries.usecases.transaction

import cats.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.ports.*

import spendthrift.domain.entities.transactions.*

import spendthrift.queries.dtos.findtransactionsbyid.*

object FindTransactionByIdUseCase:

  def make[F[_]: Sync: Trace](gateway: FindTransactionByIdGateway[F]): F[FindTransactionByIdUseCase[F]] =
    Sync[F].delay(new FindTransactionByIdUseCase[F](gateway))

end FindTransactionByIdUseCase

final class FindTransactionByIdUseCase[F[_]: FlatMap: Trace](gateway: FindTransactionByIdGateway[F]):

  def run(id: FindTransactionById): F[Option[Transaction]] =
    Trace[F].span("usecase.find-transaction-by-id") {
      for {
        _           <- Trace[F].put("id" -> id)
        transaction <- gateway.findById(id.toDomain)
        _           <- Trace[F].put("found" -> transaction.isDefined)
      } yield transaction
    }
