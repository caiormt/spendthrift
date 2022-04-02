package spendthrift.application.modules.repositories

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import skunk.*

import spendthrift.ports.*

import spendthrift.adapters.repositories.inmemory.*
import spendthrift.adapters.repositories.sql.*

object UserRepository:

  def makeInMemory[F[_]: Sync: Trace]: F[UserRepository[F]] =
    for {
      inMemoryUserRepository <- InMemoryUserRepository.make[F]
    } yield new UserRepository[F](
      inMemoryUserRepository
    )

  def makeSkunk[F[_]: Sync: Trace](sessionPool: Resource[F, Session[F]]): F[UserRepository[F]] =
    for {
      skunkUserRepository <- SkunkUserRepository.make[F](sessionPool)
    } yield new UserRepository[F](
      skunkUserRepository
    )

end UserRepository

final class UserRepository[F[_]] private (
    val registerUserGateway: RegisterUserGateway[F]
)
