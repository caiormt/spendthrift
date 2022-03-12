package spendthrift.application.modules.repositories

import cats.implicits.*

import cats.effect.*

import spendthrift.ports.*

import spendthrift.adapters.repositories.inmemory.*

object TransactionRepository:
  def make[F[_]: Sync]: F[TransactionRepository[F]] =
    for {
      inMemoryTransactionRepository <- Sync[F].delay(new InMemoryTransactionRepository[F])
    } yield new TransactionRepository[F](
      inMemoryTransactionRepository,
      inMemoryTransactionRepository
    )

final class TransactionRepository[F[_]] private (
    val registerTransactionGateway: RegisterTransactionGateway[F],
    val findTransactionByIdGateway: FindTransactionByIdGateway[F]
)
