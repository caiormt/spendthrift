package spendthrift.web.routes
package transaction

import cats.implicits.*

import cats.effect.*

import io.circe.*

import org.http4s.*
import org.http4s.circe.*
import org.http4s.implicits.*

import munit.*

import spendthrift.adapters.repositories.inmemory.*

import spendthrift.commands.usecases.transaction.*

import spendthrift.presentation.controllers.transaction.*

final class RegisterTransactionRouteSpec extends HttpRouteSuite {

  override def beforeEach(context: BeforeEach): Unit = {
    super.beforeEach(context)
    InMemoryTransactionRepository.clear()
  }

  test("Must register a new transaction") {
    val date        = Json.fromString("2022-03-12T15:30:00Z")
    val value       = Json.obj(
      "amount"   -> Json.fromBigDecimal(4.96),
      "currency" -> Json.fromString("USD")
    )
    val description = Json.fromString("Gasoline")

    val entity = Json.obj(
      "date"        -> date,
      "value"       -> value,
      "description" -> description
    )

    val gateway    = new InMemoryTransactionRepository[IO]
    val usecase    = new RegisterTransactionUseCase[IO](gateway)
    val controller = new RegisterTransactionController[IO](usecase)
    val api        = new RegisterTransactionRoute[IO](controller)

    val response = api.routes.orNotFound.run(
      Request[IO](Method.POST, uri"/transactions")
        .withEntity(entity)
    )

    checkAll[Json](response, Status.Created, f = checkRegisterTransaction(entity))
  }

  test("Must register a new transaction omitting currency") {
    val date        = Json.fromString("2022-03-12T15:30:00Z")
    val amount      = Json.fromBigDecimal(7.50)
    val value       = Json.obj(
      "amount"   -> amount,
      "currency" -> Json.fromString("BRL")
    )
    val description = Json.fromString("Gasoline")

    val entity   = Json.obj(
      "date"        -> date,
      "value"       -> Json.obj("amount" -> amount),
      "description" -> description
    )
    val expected = Json.obj(
      "date"        -> date,
      "value"       -> value,
      "description" -> description
    )

    val gateway    = new InMemoryTransactionRepository[IO]
    val usecase    = new RegisterTransactionUseCase[IO](gateway)
    val controller = new RegisterTransactionController[IO](usecase)
    val api        = new RegisterTransactionRoute[IO](controller)

    val response = api.routes.orNotFound.run(
      Request[IO](Method.POST, uri"/transactions")
        .withEntity(entity)
    )

    checkAll[Json](response, Status.Created, f = checkRegisterTransaction(expected))
  }

  test("Must register a new transaction omitting value object") {
    val date        = Json.fromString("2022-03-12T15:30:00Z")
    val amount      = Json.fromBigDecimal(7.50)
    val value       = Json.obj(
      "amount"   -> amount,
      "currency" -> Json.fromString("BRL")
    )
    val description = Json.fromString("Gasoline")

    val entity   = Json.obj(
      "date"        -> date,
      "value"       -> amount,
      "description" -> description
    )
    val expected = Json.obj(
      "date"        -> date,
      "value"       -> value,
      "description" -> description
    )

    val gateway    = new InMemoryTransactionRepository[IO]
    val usecase    = new RegisterTransactionUseCase[IO](gateway)
    val controller = new RegisterTransactionController[IO](usecase)
    val api        = new RegisterTransactionRoute[IO](controller)

    val response = api.routes.orNotFound.run(
      Request[IO](Method.POST, uri"/transactions")
        .withEntity(entity)
    )

    checkAll[Json](response, Status.Created, f = checkRegisterTransaction(expected))
  }

  private def checkRegisterTransaction(expected: Json)(using loc: Location): ResponseCheck =
    response => checkLocationHeader(uri"/transactions")(response) &> checkResponseBody(expected)(response)
}
