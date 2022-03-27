package spendthrift.commands.usecases.transaction

import cats.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.ports.*

import spendthrift.commands.dtos.registertransactions.*

import spendthrift.domain.entities.transactions.*

import spendthrift.effects.generators.*

object RegisterTransactionUseCase:

  def make[F[_]: Sync: UUIDGen: Trace](gateway: RegisterTransactionGateway[F]): F[RegisterTransactionUseCase[F]] =
    Sync[F].delay(new RegisterTransactionUseCase[F](gateway))

end RegisterTransactionUseCase

final class RegisterTransactionUseCase[F[_]: Monad: UUIDGen: Trace](gateway: RegisterTransactionGateway[F]):

  def run(command: RegisterTransaction): F[Transaction] =
    Trace[F].span("usecase.register-transaction") {
      for {
        id          <- UUIDGen.randomUUID[F].map(TransactionId.apply)
        transaction <- Applicative[F].pure(command.toDomain(id))
        _           <- gateway.register(transaction)
      } yield transaction
    }
