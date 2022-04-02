package spendthrift.commands.usecases.user

import cats.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.ports.*

import spendthrift.commands.dtos.registerusers.*

import spendthrift.domain.entities.users.*

import spendthrift.effects.generators.*

object RegisterUserUseCase:

  def make[F[_]: Sync: UUIDGen: Trace](gateway: RegisterUserGateway[F]): F[RegisterUserUseCase[F]] =
    Sync[F].delay(new RegisterUserUseCase[F](gateway))

end RegisterUserUseCase

final class RegisterUserUseCase[F[_]: Monad: UUIDGen: Trace](gateway: RegisterUserGateway[F]):

  def run(command: RegisterUser): F[User] =
    Trace[F].span("usecase.register-user") {
      for {
        id   <- UUIDGen.randomUUID[F].map(UserId.apply)
        user <- Applicative[F].pure(command.toDomain(id))
        _    <- gateway.register(user)
      } yield user
    }
