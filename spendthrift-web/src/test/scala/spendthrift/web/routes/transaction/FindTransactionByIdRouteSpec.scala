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

import spendthrift.domain.entities.transactions.*

import spendthrift.presentation.controllers.transaction.*

import spendthrift.queries.usecases.transaction.*

import java.time.*
import java.time.format.*
import java.util.*

final class FindTransactionByIdRouteSpec extends HttpRouteSuite {

  override def beforeEach(context: BeforeEach): Unit = {
    super.beforeEach(context)
    InMemoryTransactionRepository.clear()
  }

  test("Must return nothing when transaction not found") {
    val nilUUID = new UUID(0L, 0L)

    val gateway    = new InMemoryTransactionRepository[IO]
    val usecase    = new FindTransactionByIdUseCase[IO](gateway)
    val controller = new FindTransactionByIdController[IO](usecase)
    val api        = new FindTransactionByIdRoute[IO](controller)

    val response = api.routes.orNotFound.run(
      Request[IO](Method.GET, uri"/transactions" / nilUUID.toString)
    )

    check[Json](response, Status.NotFound)
  }

  test("Must return transaction found") {
    val nilUUID = new UUID(0L, 0L)
    val date    = ZonedDateTime.of(LocalDate.now, LocalTime.MIDNIGHT, ZoneOffset.UTC)
    val value   = 7.50

    val transaction = Transaction(
      TransactionId(nilUUID),
      TransactionDate(date),
      TransactionValue(value),
      TransactionDescription("Gasoline")
    )

    InMemoryTransactionRepository.save(transaction)

    val expected = Json.obj(
      "id"          -> Json.fromString(nilUUID.toString),
      "date"        -> Json.fromString(date.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)),
      "value"       -> Json.obj(
        "amount"   -> Json.fromBigDecimal(value),
        "currency" -> Json.fromString("BRL")
      ),
      "description" -> Json.fromString("Gasoline")
    )

    val gateway    = new InMemoryTransactionRepository[IO]
    val usecase    = new FindTransactionByIdUseCase[IO](gateway)
    val controller = new FindTransactionByIdController[IO](usecase)
    val api        = new FindTransactionByIdRoute[IO](controller)

    val response = api.routes.orNotFound.run(
      Request[IO](Method.GET, uri"/transactions" / nilUUID.toString)
    )

    check[Json](response, Status.Ok, expected.some)
  }
}
