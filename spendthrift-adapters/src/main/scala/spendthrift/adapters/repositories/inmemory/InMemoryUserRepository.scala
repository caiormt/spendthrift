package spendthrift.adapters.repositories.inmemory

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import spendthrift.ports.*

import spendthrift.domain.entities.users.*

import scala.collection.concurrent.*

object InMemoryUserRepository:
  final private lazy val database = TrieMap.empty[UserId, User]

  def make[F[_]: Sync: Trace]: F[InMemoryUserRepository[F]] =
    Sync[F].delay(new InMemoryUserRepository[F])

end InMemoryUserRepository

final class InMemoryUserRepository[F[_]: Sync: Trace] extends RegisterUserGateway[F]:

  import InMemoryUserRepository.*

  override def register(user: User): F[Unit] =
    Trace[F].span("repository.user.register") {
      Sync[F].delay(database.put(user.id, user)).void
    }
