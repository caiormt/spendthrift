package spendthrift.presentation.views

import cats.*
import cats.derived.*

import spendthrift.domain.entities.transactions.*

import spendthrift.queries.dtos.{ findtransactionsbyid => query }

import java.util.*

object findtransactionsbyid:

  opaque type FindTransactionById = UUID

  extension (id: FindTransactionById)
    def toQuery: query.FindTransactionById =
      query.FindTransactionById(id)

  object FindTransactionById:
    def apply(id: UUID): FindTransactionById = id
