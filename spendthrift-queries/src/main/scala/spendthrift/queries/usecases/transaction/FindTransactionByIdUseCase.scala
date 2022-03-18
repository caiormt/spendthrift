package spendthrift.queries.usecases.transaction

import cats.effect.*

import spendthrift.ports.*

import spendthrift.domain.entities.transactions.*

import spendthrift.queries.dtos.findtransactionsbyid.*

object FindTransactionByIdUseCase:

  def make[F[_]: Sync](gateway: FindTransactionByIdGateway[F]): F[FindTransactionByIdUseCase[F]] =
    Sync[F].delay(new FindTransactionByIdUseCase[F](gateway))

end FindTransactionByIdUseCase

final class FindTransactionByIdUseCase[F[_]](gateway: FindTransactionByIdGateway[F]):

  def run(id: FindTransactionById): F[Option[Transaction]] =
    gateway.findById(id.toDomain)
