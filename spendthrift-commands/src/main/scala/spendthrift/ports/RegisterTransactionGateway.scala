package spendthrift.ports

import spendthrift.domain.entities.transactions.*

trait RegisterTransactionGateway[F[_]]:
  def register(transaction: Transaction): F[Unit]
