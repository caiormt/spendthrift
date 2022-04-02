package spendthrift.application.modules.usecases

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.application.modules.*
import spendthrift.application.modules.repositories.*

import spendthrift.commands.usecases.user.*

object UserUseCase:

  def make[F[_]: Sync: Trace](repositories: UserRepository[F]): F[UserUseCase[F]] = {
    import repositories.*

    for {
      registerUserUseCase <- RegisterUserUseCase.make[F](registerUserGateway)
    } yield new UserUseCase[F](
      registerUserUseCase
    )
  }

end UserUseCase

final class UserUseCase[F[_]] private (
    val registerUserUseCase: RegisterUserUseCase[F]
)
