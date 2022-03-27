package spendthrift.queries.dtos

import natchez.*

import spendthrift.domain.entities.transactions.*

import java.util.*

object findtransactionsbyid:

  opaque type FindTransactionById = UUID

  given Conversion[FindTransactionById, TraceValue] with
    override def apply(id: FindTransactionById): TraceValue =
      TraceValue.StringValue(id.toString)

  extension (id: FindTransactionById)
    def toDomain: TransactionId =
      TransactionId(id)

  object FindTransactionById:
    def apply(id: UUID): FindTransactionById = id
