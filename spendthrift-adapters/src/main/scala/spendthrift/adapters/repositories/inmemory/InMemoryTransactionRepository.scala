package spendthrift.adapters.repositories.inmemory

import cats.implicits.*

import cats.effect.*

import spendthrift.ports.*

import spendthrift.domain.entities.transactions.*

import scala.collection.concurrent.*

object InMemoryTransactionRepository:
  final private lazy val database = TrieMap.empty[TransactionId, Transaction]

  def clear(): Unit =
    database.clear()

  def save(transaction: Transaction): Unit =
    database.put(transaction.id, transaction)

final class InMemoryTransactionRepository[F[_]: Sync]
    extends RegisterTransactionGateway[F]
      with FindTransactionByIdGateway[F]:

  import InMemoryTransactionRepository.*

  override def register(transaction: Transaction): F[Unit] =
    Sync[F].delay(database.put(transaction.id, transaction)).void

  override def findById(id: TransactionId): F[Option[Transaction]] =
    Sync[F].delay(database.get(id))
