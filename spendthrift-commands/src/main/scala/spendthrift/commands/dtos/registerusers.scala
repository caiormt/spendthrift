package spendthrift.commands.dtos

import cats.*
import cats.derived.*

import spendthrift.domain.entities.users.*

object registerusers:

  extension (r: RegisterUser)
    def toDomain(id: UserId): User =
      User(
        id
      )

  // format: off
  final case class RegisterUser() derives Eq, Show
  // format: on
