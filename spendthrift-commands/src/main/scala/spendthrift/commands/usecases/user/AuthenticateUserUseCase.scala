package spendthrift.commands.usecases.user

import cats.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.ports.*

import spendthrift.commands.dtos.authenticateusers.*

import spendthrift.domain.entities.users.*

object AuthenticateUserUseCase:

  def make[F[_]: Sync: Trace](jwtGateway: AuthenticateUserJwtGateway[F]): F[AuthenticateUserUseCase[F]] =
    Sync[F].delay(new AuthenticateUserUseCase[F](jwtGateway))

end AuthenticateUserUseCase

final class AuthenticateUserUseCase[F[_]: Monad: Trace](jwtGateway: AuthenticateUserJwtGateway[F]):

  def run(command: AuthenticateUser): F[Principal] =
    Trace[F].span("usecase.authenticate-user") {
      command match {
        case AuthenticateUser.Jwt(token) =>
          Trace[F].span("usecase.authenticate-user.token") {
            jwtGateway.authenticate(token)
          }
      }
    }
