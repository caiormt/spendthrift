package spendthrift.application.http

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import org.http4s.*

import spendthrift.application.modules.controllers.*

import spendthrift.web.routes.user.*

object UserRoutes:

  private val RESOURCE_PATH_R = "^/users".r.unanchored

  def classify(renderedUri: String): Option[String] =
    renderedUri match {
      case RESOURCE_PATH_R() => "/users".some
      case _                 => none
    }

end UserRoutes

final class UserRoutes[F[_]: Concurrent: Trace](controllers: UserController[F]):

  import controllers.*

  private val registerUserRoute =
    new RegisterUserRoute[F](registerUserController).routes

  final val routes: HttpRoutes[F] =
    registerUserRoute

end UserRoutes
