package spendthrift.commands.usecases.transaction

import cats.implicits.*

import cats.effect.*

import spendthrift.ports.*

import spendthrift.commands.dtos.registertransactions.*

import spendthrift.domain.entities.transactions.*

import spendthrift.effects.generators.*

final class RegisterTransactionUseCase[F[_]: Sync: UUIDGen](gateway: RegisterTransactionGateway[F]):

  def run(command: RegisterTransaction): F[Transaction] =
    for {
      id          <- UUIDGen.randomUUID[F].map(TransactionId.apply)
      transaction <- Sync[F].delay(command.toDomain(id))
      _           <- gateway.register(transaction)
    } yield transaction
