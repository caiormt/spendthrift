package spendthrift.presentation.views

import cats.*
import cats.derived.*

import io.circe.*

import io.chrisdavenport.cats.time.*

import spendthrift.commands.dtos.{ registertransactions => cmd }

import spendthrift.commons.*

import java.time.*

object registertransactions:

  import Squants.given

  opaque type RegisterTransactionDate        = ZonedDateTime
  opaque type RegisterTransactionValue       = squants.market.Money
  opaque type RegisterTransactionDescription = String

  extension (r: RegisterTransaction)
    def toCommand: cmd.RegisterTransaction =
      cmd.RegisterTransaction(
        cmd.RegisterTransactionDate(r.date),
        cmd.RegisterTransactionValue(r.value),
        cmd.RegisterTransactionDescription(r.description)
      )

  // format: off
  final case class RegisterTransaction(
      date: RegisterTransactionDate,
      value: RegisterTransactionValue,
      description: RegisterTransactionDescription
  ) derives Eq, Show, Decoder
  // format: on
