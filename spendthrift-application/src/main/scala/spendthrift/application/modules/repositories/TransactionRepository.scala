package spendthrift.application.modules.repositories

import cats.implicits.*

import cats.effect.*

import skunk.*

import spendthrift.ports.*

import spendthrift.adapters.repositories.inmemory.*
import spendthrift.adapters.repositories.sql.*

object TransactionRepository:
  def makeInMemory[F[_]: Sync]: F[TransactionRepository[F]] =
    for {
      inMemoryTransactionRepository <- Sync[F].delay(new InMemoryTransactionRepository[F])
    } yield new TransactionRepository[F](
      inMemoryTransactionRepository,
      inMemoryTransactionRepository
    )

  def makeSkunk[F[_]: Sync](sessionPool: Resource[F, Session[F]]): F[TransactionRepository[F]] =
    for {
      skunkTransactionRepository <- Sync[F].delay(new SkunkTransactionRepository[F](sessionPool))
    } yield new TransactionRepository[F](
      skunkTransactionRepository,
      skunkTransactionRepository
    )

final class TransactionRepository[F[_]] private (
    val registerTransactionGateway: RegisterTransactionGateway[F],
    val findTransactionByIdGateway: FindTransactionByIdGateway[F]
)
