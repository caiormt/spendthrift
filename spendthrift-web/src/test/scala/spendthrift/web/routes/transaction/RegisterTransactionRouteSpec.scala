package spendthrift.web.routes
package transaction

import cats.implicits.*

import cats.effect.*

import io.circe.*

import natchez.Trace.Implicits.noop

import org.http4s.*
import org.http4s.circe.*
import org.http4s.implicits.*

import squants.market.*

import munit.*

import spendthrift.ports.*

import spendthrift.commands.usecases.transaction.*

import spendthrift.domain.entities.transactions.*
import spendthrift.domain.entities.users.*

import spendthrift.effects.generators.*

import spendthrift.presentation.controllers.transaction.*

import java.time.*
import java.time.format.*
import java.util.*

final class RegisterTransactionRouteSpec extends HttpRouteSuite {

  test("Must register a new transaction") {
    val nilUUID     = new UUID(0L, 0L)
    val date        = ZonedDateTime.of(LocalDate.now, LocalTime.MIDNIGHT, ZoneOffset.UTC)
    val value       = 4.96
    val description = "Gasoline"

    val entity = Json.obj(
      "date"        -> Json.fromString(date.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)),
      "value"       -> Json.obj(
        "amount"   -> Json.fromBigDecimal(value),
        "currency" -> Json.fromString("USD")
      ),
      "description" -> Json.fromString(description)
    )

    val expectedTransaction = Transaction(
      TransactionId(nilUUID),
      TransactionDate(date),
      TransactionValue(Money(value, USD)),
      TransactionDescription(description)
    )

    given UUIDGen[IO] with
      override def randomUUID: IO[UUID] =
        IO.delay(nilUUID)

    val gateway    = new RegisterTransactionGateway[IO] {
      override def register(transaction: Transaction): IO[Unit] =
        IO(assertEquals(transaction, expectedTransaction, "Must receive transaction"))
    }
    val usecase    = new RegisterTransactionUseCase[IO](gateway)
    val controller = new RegisterTransactionController[IO](usecase)
    val api        = new RegisterTransactionRoute[IO](controller)

    val response = api.routes.orNotFound.run(
      AuthedRequest(
        UserPrincipal(UserId(nilUUID)),
        Request[IO](Method.POST, uri"/transactions").withEntity(entity)
      )
    )

    checkAll[Json](response, Status.Created, f = checkRegisterTransaction(entity))
  }

  test("Must register a new transaction omitting currency") {
    val nilUUID     = new UUID(0L, 0L)
    val date        = ZonedDateTime.of(LocalDate.now, LocalTime.MIDNIGHT, ZoneOffset.UTC)
    val value       = 7.50
    val description = "Gasoline"

    val entity = Json.obj(
      "date"        -> Json.fromString(date.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)),
      "value"       -> Json.obj("amount" -> Json.fromBigDecimal(value)),
      "description" -> Json.fromString(description)
    )

    val expected = Json.obj(
      "date"        -> Json.fromString(date.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)),
      "value"       -> Json.obj(
        "amount"   -> Json.fromBigDecimal(value),
        "currency" -> Json.fromString("BRL")
      ),
      "description" -> Json.fromString(description)
    )

    val expectedTransaction = Transaction(
      TransactionId(nilUUID),
      TransactionDate(date),
      TransactionValue(value),
      TransactionDescription(description)
    )

    given UUIDGen[IO] with
      override def randomUUID: IO[UUID] =
        IO.delay(nilUUID)

    val gateway    = new RegisterTransactionGateway[IO] {
      override def register(transaction: Transaction): IO[Unit] =
        IO(assertEquals(transaction, expectedTransaction, "Must receive transaction"))
    }
    val usecase    = new RegisterTransactionUseCase[IO](gateway)
    val controller = new RegisterTransactionController[IO](usecase)
    val api        = new RegisterTransactionRoute[IO](controller)

    val response = api.routes.orNotFound.run(
      AuthedRequest(
        UserPrincipal(UserId(nilUUID)),
        Request[IO](Method.POST, uri"/transactions").withEntity(entity)
      )
    )

    checkAll[Json](response, Status.Created, f = checkRegisterTransaction(expected))
  }

  test("Must register a new transaction omitting value object") {
    val nilUUID     = new UUID(0L, 0L)
    val date        = ZonedDateTime.of(LocalDate.now, LocalTime.MIDNIGHT, ZoneOffset.UTC)
    val value       = 7.50
    val description = "Gasoline"

    val entity = Json.obj(
      "date"        -> Json.fromString(date.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)),
      "value"       -> Json.fromBigDecimal(value),
      "description" -> Json.fromString(description)
    )

    val expected = Json.obj(
      "date"        -> Json.fromString(date.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)),
      "value"       -> Json.obj(
        "amount"   -> Json.fromBigDecimal(value),
        "currency" -> Json.fromString("BRL")
      ),
      "description" -> Json.fromString(description)
    )

    val expectedTransaction = Transaction(
      TransactionId(nilUUID),
      TransactionDate(date),
      TransactionValue(value),
      TransactionDescription(description)
    )

    given UUIDGen[IO] with
      override def randomUUID: IO[UUID] =
        IO.delay(nilUUID)

    val gateway    = new RegisterTransactionGateway[IO] {
      override def register(transaction: Transaction): IO[Unit] =
        IO(assertEquals(transaction, expectedTransaction, "Must receive transaction"))
    }
    val usecase    = new RegisterTransactionUseCase[IO](gateway)
    val controller = new RegisterTransactionController[IO](usecase)
    val api        = new RegisterTransactionRoute[IO](controller)

    val response = api.routes.orNotFound.run(
      AuthedRequest(
        UserPrincipal(UserId(nilUUID)),
        Request[IO](Method.POST, uri"/transactions").withEntity(entity)
      )
    )

    checkAll[Json](response, Status.Created, f = checkRegisterTransaction(expected))
  }

  private def checkRegisterTransaction(expected: Json)(using loc: Location): ResponseCheck =
    response => checkLocationHeader(uri"/transactions")(response) &> checkResponseBody(expected)(response)
}
