package spendthrift.queries.dtos

import spendthrift.domain.entities.transactions.*

import java.util.*

object findtransactionsbyid:

  opaque type FindTransactionById = UUID

  extension (id: FindTransactionById)
    def toDomain: TransactionId =
      TransactionId(id)

  object FindTransactionById:
    def apply(id: UUID): FindTransactionById = id
