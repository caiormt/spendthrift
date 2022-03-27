package spendthrift.application.modules.repositories

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import skunk.*

import spendthrift.ports.*

import spendthrift.adapters.repositories.inmemory.*
import spendthrift.adapters.repositories.sql.*

object TransactionRepository:

  def makeInMemory[F[_]: Sync: Trace]: F[TransactionRepository[F]] =
    for {
      inMemoryTransactionRepository <- InMemoryTransactionRepository.make[F]
    } yield new TransactionRepository[F](
      inMemoryTransactionRepository,
      inMemoryTransactionRepository
    )

  def makeSkunk[F[_]: Sync: Trace](sessionPool: Resource[F, Session[F]]): F[TransactionRepository[F]] =
    for {
      skunkTransactionRepository <- SkunkTransactionRepository.make[F](sessionPool)
    } yield new TransactionRepository[F](
      skunkTransactionRepository,
      skunkTransactionRepository
    )

end TransactionRepository

final class TransactionRepository[F[_]] private (
    val registerTransactionGateway: RegisterTransactionGateway[F],
    val findTransactionByIdGateway: FindTransactionByIdGateway[F]
)
