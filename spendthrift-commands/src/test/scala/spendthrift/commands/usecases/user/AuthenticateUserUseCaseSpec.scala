package spendthrift.commands.usecases.user

import cats.implicits.*

import cats.effect.*

import natchez.Trace.Implicits.noop

import munit.*

import spendthrift.ports.*

import spendthrift.commands.dtos.authenticateusers.*

import spendthrift.domain.entities.users.*

import java.util.*

final class AuthenticateUserUseCaseSpec extends CatsEffectSuite {

  test("Must authenticate using JWT") {
    val nilUUID = new UUID(0L, 0L)
    val token   = "<TOKEN>"

    val command   = AuthenticateUser.Jwt(token)
    val principal = UserPrincipal(
      UserId(nilUUID)
    )

    val gateway = new AuthenticateUserJwtGateway[IO] {
      override def authenticate(token: String): IO[Principal] =
        IO(assertEquals(token, token, "Must authenticate from received token")) *> IO.delay(principal)
    }
    val usecase = new AuthenticateUserUseCase[IO](gateway)

    assertIO(usecase.run(command), principal, "Must receive Principal from JWT Token")
  }
}
