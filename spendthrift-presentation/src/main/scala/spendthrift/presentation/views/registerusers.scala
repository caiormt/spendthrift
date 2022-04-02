package spendthrift.presentation.views

import cats.*
import cats.derived.*

import io.circe.*

import spendthrift.commands.dtos.{ registerusers => cmd }

object registerusers:

  extension (r: RegisterUser)
    def toCommand: cmd.RegisterUser =
      cmd.RegisterUser()

  // format: off
  final case class RegisterUser() derives Eq, Show, Decoder
  // format: on
