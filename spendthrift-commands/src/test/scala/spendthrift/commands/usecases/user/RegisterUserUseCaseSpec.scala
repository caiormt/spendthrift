package spendthrift.commands.usecases.user

import cats.implicits.*

import cats.effect.*

import natchez.Trace.Implicits.noop

import munit.*

import spendthrift.ports.*

import spendthrift.commands.dtos.registerusers.*

import spendthrift.domain.entities.users.*

import spendthrift.effects.generators.*

import java.util.*

final class RegisterUserUseCaseSpec extends CatsEffectSuite {

  test("Must generate random UserId and call gateway to register") {
    val nilUUID = new UUID(0L, 0L)

    val command  = RegisterUser()
    val expected = User(
      UserId(nilUUID)
    )

    given UUIDGen[IO] with
      override def randomUUID: IO[UUID] =
        IO.delay(nilUUID)

    val gateway = new RegisterUserGateway[IO] {
      override def register(user: User): IO[Unit] =
        IO(assertEquals(user, expected, "Must transform command into domain with generated id"))
    }
    val usecase = new RegisterUserUseCase[IO](gateway)

    assertIO(usecase.run(command), expected, "Must receive user with generated id")
  }
}
