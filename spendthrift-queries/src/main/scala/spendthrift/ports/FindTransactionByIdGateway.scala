package spendthrift.ports

import spendthrift.domain.entities.transactions.*

trait FindTransactionByIdGateway[F[_]]:
  def findById(id: TransactionId): F[Option[Transaction]]
