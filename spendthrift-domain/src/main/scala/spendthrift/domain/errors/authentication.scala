package spendthrift.domain.errors

import cats.*
import cats.derived.*

import scala.util.control.*

object authentication:

  sealed trait AuthenticationError extends RuntimeException with NoStackTrace derives Eq, Show

  case object ExpiredJwtToken extends AuthenticationError
  case class MalformedJwtToken(message: String) extends AuthenticationError {
    override def getMessage: String = message
  }
  case class InvalidJwtToken(message: String) extends AuthenticationError {
    override def getMessage: String = message
  }
