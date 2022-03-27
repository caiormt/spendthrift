package spendthrift.adapters.repositories.inmemory

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.ports.*

import spendthrift.domain.entities.transactions.*

import scala.collection.concurrent.*

object InMemoryTransactionRepository:
  final private lazy val database = TrieMap.empty[TransactionId, Transaction]

  def make[F[_]: Sync: Trace]: F[InMemoryTransactionRepository[F]] =
    Sync[F].delay(new InMemoryTransactionRepository[F])

end InMemoryTransactionRepository

final class InMemoryTransactionRepository[F[_]: Sync: Trace]
    extends RegisterTransactionGateway[F]
      with FindTransactionByIdGateway[F]:

  import InMemoryTransactionRepository.*

  override def register(transaction: Transaction): F[Unit] =
    Trace[F].span("repository.transaction.register") {
      Sync[F].delay(database.put(transaction.id, transaction)).void
    }

  override def findById(id: TransactionId): F[Option[Transaction]] =
    Trace[F].span("repository.transaction.find-by-id") {
      Sync[F].delay(database.get(id))
    }
