package spendthrift.commands.dtos

import cats.*
import cats.derived.*

import io.chrisdavenport.cats.time.*

import spendthrift.commons.*

import spendthrift.domain.entities.transactions.*

import java.time.*
import java.util.*

object registertransactions:

  import Squants.given

  opaque type RegisterTransactionDate        = ZonedDateTime
  opaque type RegisterTransactionValue       = squants.market.Money
  opaque type RegisterTransactionDescription = String

  extension (r: RegisterTransaction)
    def toDomain(id: TransactionId): Transaction =
      Transaction(
        id,
        TransactionDate(r.date),
        TransactionValue(r.value),
        TransactionDescription(r.description)
      )

  object RegisterTransactionDate:
    def apply(date: ZonedDateTime): RegisterTransactionDate = date

  object RegisterTransactionValue:
    def apply(money: squants.market.Money): RegisterTransactionValue = money

  object RegisterTransactionDescription:
    def apply(description: String): RegisterTransactionDescription = description

  // format: off
  final case class RegisterTransaction(
      date: RegisterTransactionDate,
      value: RegisterTransactionValue,
      description: RegisterTransactionDescription
  ) derives Eq, Show
  // format: on
