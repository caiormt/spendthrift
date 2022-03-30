package spendthrift.domain.entities

import cats.*
import cats.derived.*

import java.util.*

object users:

  opaque type UserId = UUID

  object UserId:
    def apply(id: UUID): UserId = id

  // format: off
  final case class User(
      id: UserId
  ) derives Eq, Show
  // format: on
