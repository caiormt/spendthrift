package spendthrift.presentation.views

import cats.*
import cats.derived.*

import io.circe.*

import io.chrisdavenport.cats.time.*

import spendthrift.commons.*

import spendthrift.domain.entities.{ transactions => domain }

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
    def show: String = Show[UUID].show(id)
  // format: on

  extension (t: domain.Transaction)
    def toView: Transaction =
      Transaction(
        t.id.value,
        t.date.value,
        t.value.value,
        t.description.value
      )

  // format: off
  final case class Transaction(
      id: TransactionId,
      date: TransactionDate,
      value: TransactionValue,
      description: TransactionDescription
  ) derives Eq, Show, Encoder.AsObject
  // format: on
