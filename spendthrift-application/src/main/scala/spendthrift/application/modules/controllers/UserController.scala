package spendthrift.application.modules.controllers

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.application.modules.*
import spendthrift.application.modules.usecases.*

import spendthrift.presentation.controllers.user.*

object UserController:

  def make[F[_]: Sync: Trace](usecases: UserUseCase[F]): F[UserController[F]] = {
    import usecases.*

    for {
      registerUserController <- RegisterUserController.make[F](registerUserUseCase)
    } yield new UserController[F](
      registerUserController
    )
  }

end UserController

final class UserController[F[_]] private (
    val registerUserController: RegisterUserController[F]
)
