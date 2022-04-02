package spendthrift.web.routes
package user

import cats.implicits.*

import cats.effect.*

import io.circe.*

import natchez.Trace.Implicits.noop

import org.http4s.*
import org.http4s.circe.*
import org.http4s.implicits.*

import munit.*

import spendthrift.ports.*

import spendthrift.commands.usecases.user.*

import spendthrift.domain.entities.users.*

import spendthrift.effects.generators.*

import spendthrift.presentation.controllers.user.*

import java.util.*

final class RegisterUserRouteSpec extends HttpRouteSuite {

  test("Must register a new user") {
    val nilUUID = new UUID(0L, 0L)

    val expectedUser = User(
      UserId(nilUUID)
    )

    given UUIDGen[IO] with
      override def randomUUID: IO[UUID] =
        IO.delay(nilUUID)

    val gateway    = new RegisterUserGateway[IO] {
      override def register(user: User): IO[Unit] =
        IO(assertEquals(user, expectedUser, "Must receive user"))
    }
    val usecase    = new RegisterUserUseCase[IO](gateway)
    val controller = new RegisterUserController[IO](usecase)
    val api        = new RegisterUserRoute[IO](controller)

    val response = api.routes.orNotFound.run(
      Request[IO](Method.POST, uri"/users")
    )

    checkAll[Json](response, Status.Created, f = checkLocationHeader(uri"/users"))
  }
}
