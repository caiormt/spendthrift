package spendthrift.queries.usecases.transaction

import spendthrift.ports.*

import spendthrift.domain.entities.transactions.*

import spendthrift.queries.dtos.findtransactionsbyid.*

final class FindTransactionByIdUseCase[F[_]](gateway: FindTransactionByIdGateway[F]):

  def run(id: FindTransactionById): F[Option[Transaction]] =
    gateway.findById(id.toDomain)
