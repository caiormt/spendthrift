package spendthrift.adapters.repositories.sql

import cats.implicits.*

import cats.effect.*

import natchez.Trace.Implicits.noop

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

import munit.*

import spendthrift.domain.entities.{ users => d }

final class SkunkUserRepositorySpec extends SqlSuite {

  override val cleanUpTables: List[String] =
    List(SkunkUserSchema.tableName)

  test("should persist new user") {
    val uId = new java.util.UUID(0L, 0L)

    val user = d.User(
      d.UserId(uId)
    )

    val repository = new SkunkUserRepository[IO](sessionPool)
    val register   = repository.register(user)

    val sql    = sql"SELECT id FROM USERS WHERE id = $uuid"
    val query  = sql.query(uuid)
    val result = session.prepare(query).use(_.option(uId))

    register *> result.map {
      case None     =>
        fail("Should retrieve persisted user")
      case Some(id) =>
        assertEquals(id, uId)
    }
  }
}
