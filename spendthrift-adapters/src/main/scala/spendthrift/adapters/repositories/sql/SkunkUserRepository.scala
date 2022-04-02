package spendthrift.adapters.repositories.sql

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

import spendthrift.ports.*

import spendthrift.commons.*

import spendthrift.domain.entities.{ users => d }

object SkunkUserRepository:

  def make[F[_]: Sync: Trace](sessionPool: Resource[F, Session[F]]): F[SkunkUserRepository[F]] =
    Sync[F].delay(new SkunkUserRepository[F](sessionPool))

end SkunkUserRepository

final class SkunkUserRepository[F[_]: Sync: Trace](sessionPool: Resource[F, Session[F]]) extends RegisterUserGateway[F]:

  import SkunkUserSchema.*

  private val INSERT_USER_COMMAND: Command[d.User] =
    sql"INSERT INTO #$tableName(id) VALUES($codec)".command

  override def register(user: d.User): F[Unit] =
    Trace[F].span("repository.user.register") {
      sessionPool.use { session =>
        session.prepare(INSERT_USER_COMMAND).use(_.execute(user)).void
      }
    }

end SkunkUserRepository

object SkunkUserSchema:

  import Squants.given

  val tableName: String = "USERS"

  val id: Codec[d.UserId] =
    uuid.imap(d.UserId.apply)(_.value)

  val codec: Codec[d.User] =
    id.gimap[d.User]

end SkunkUserSchema
