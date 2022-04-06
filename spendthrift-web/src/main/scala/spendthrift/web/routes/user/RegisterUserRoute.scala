package spendthrift.web.routes.user

import cats.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.*
import org.http4s.headers.*
import org.http4s.implicits.*

import spendthrift.domain.entities.users.*

import spendthrift.presentation.controllers.user.*
import spendthrift.presentation.views.registerusers.*

import spendthrift.web.codec.given

final class RegisterUserRoute[F[_]: MonadThrow: JsonDecoder: Trace](controller: RegisterUserController[F])
    extends Http4sDsl[F]:

  final val routes: AuthedRoutes[Principal, F] = AuthedRoutes.of[Principal, F] {
    case POST -> Root / "users" as user =>
      Trace[F].span("routes.register-user") {
        controller.run(RegisterUser()).flatMap { user =>
          Created(user, Location(uri"/users" / user.id.show))
        }
      }
  }
