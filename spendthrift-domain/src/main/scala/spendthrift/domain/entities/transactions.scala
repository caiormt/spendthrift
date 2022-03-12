package spendthrift.domain.entities

import cats.*
import cats.derived.*

import io.chrisdavenport.cats.time.*

import squants.market.*

import spendthrift.commons.*

import java.time.*
import java.util.*

object transactions:

  import Squants.given

  opaque type TransactionId          = UUID
  opaque type TransactionDate        = ZonedDateTime
  opaque type TransactionValue       = squants.market.Money
  opaque type TransactionDescription = String

  // format: off
  extension (id: TransactionId)
    def value: UUID = id

  extension (date: TransactionDate)
    def value: ZonedDateTime = date

  extension (value: TransactionValue)
    def value: squants.market.Money = value

  extension (description: TransactionDescription)
    def value: String = description
  // format: on

  object TransactionId:
    def apply(id: UUID): TransactionId = id

  object TransactionDate:
    def apply(date: ZonedDateTime): TransactionDate = date

    def now(zoneId: ZoneId = ZoneOffset.UTC): TransactionDate =
      ZonedDateTime.now(zoneId)

  object TransactionValue:
    def apply(money: squants.market.Money): TransactionValue = money

    def apply(amount: BigDecimal, currency: squants.market.Currency = BRL): TransactionValue =
      Money(amount, currency)

  object TransactionDescription:
    def apply(description: String): TransactionDescription = description

  // format: off
  final case class Transaction(
      id: TransactionId,
      date: TransactionDate,
      value: TransactionValue,
      description: TransactionDescription
  ) derives Eq, Show
  // format: on
