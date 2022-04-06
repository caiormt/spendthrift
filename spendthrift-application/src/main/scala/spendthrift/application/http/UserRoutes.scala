package spendthrift.application.http

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import org.http4s.*

import spendthrift.application.modules.controllers.*

import spendthrift.domain.entities.users.*

import spendthrift.web.routes.user.*

object UserRoutes:

  private val RESOURCE_PATH_R =
    "/users/?".r

  def classify[F[_]: Sync](request: Request[F]): F[Option[String]] =
    Sync[F].blocking {
      request.uri.renderString match {
        case RESOURCE_PATH_R() => "/users".some
        case _                 => none
      }
    }

end UserRoutes

final class UserRoutes[F[_]: Concurrent: Trace](controllers: UserController[F]):

  import controllers.*

  private val registerUserRoute =
    new RegisterUserRoute[F](registerUserController).routes

  final val authedRoutes: AuthedRoutes[Principal, F] =
    registerUserRoute

end UserRoutes
