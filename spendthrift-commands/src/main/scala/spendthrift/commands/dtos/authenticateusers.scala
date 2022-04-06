package spendthrift.commands.dtos

import cats.*
import cats.derived.*

object authenticateusers:

  enum AuthenticateUser derives Eq, Show:
    case Jwt(token: String)
