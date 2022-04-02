package spendthrift.presentation.views

import cats.*
import cats.derived.*

import io.circe.*

import spendthrift.domain.entities.{ users => domain }

import java.util.*

object users:

  opaque type UserId = UUID

  // format: off
  extension (id: UserId)
    def show: String = Show[UUID].show(id)
  // format: on

  extension (u: domain.User)
    def toView: User =
      User(
        u.id.value
      )

  // format: off
  final case class User(
      id: UserId
  ) derives Eq, Show, Encoder.AsObject
  // format: on
