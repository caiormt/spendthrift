package spendthrift.presentation.controllers.user

import cats.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.commands.usecases.user.*

import spendthrift.presentation.views.registerusers.*
import spendthrift.presentation.views.users.*

object RegisterUserController:

  def make[F[_]: Sync: Trace](usecase: RegisterUserUseCase[F]): F[RegisterUserController[F]] =
    Sync[F].delay(new RegisterUserController[F](usecase))

end RegisterUserController

final class RegisterUserController[F[_]: Monad: Trace](usecase: RegisterUserUseCase[F]):

  def run(view: RegisterUser): F[User] =
    Trace[F].span("controller.register-user") {
      for {
        command <- Applicative[F].pure(view.toCommand)
        user    <- usecase.run(command)
      } yield user.toView
    }
