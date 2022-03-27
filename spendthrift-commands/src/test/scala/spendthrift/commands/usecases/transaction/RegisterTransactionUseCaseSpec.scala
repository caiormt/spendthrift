package spendthrift.commands.usecases.transaction

import cats.*
import cats.implicits.*

import cats.effect.*

import natchez.Trace.Implicits.noop

import squants.market.*

import munit.*

import spendthrift.ports.*

import spendthrift.commands.dtos.registertransactions.*

import spendthrift.domain.entities.transactions.*

import spendthrift.effects.generators.*

import java.time.*
import java.util.*

final class RegisterTransactionUseCaseSpec extends CatsEffectSuite {

  test("Must generate random TransactionId and call gateway to register") {
    val nilUUID     = new UUID(0L, 0L)
    val date        = ZonedDateTime.now(ZoneOffset.UTC)
    val value       = Money(7.50, BRL)
    val description = "Gasoline"

    val command  = RegisterTransaction(
      RegisterTransactionDate(date),
      RegisterTransactionValue(value),
      RegisterTransactionDescription(description)
    )
    val expected = Transaction(
      TransactionId(nilUUID),
      TransactionDate(date),
      TransactionValue(value),
      TransactionDescription(description)
    )

    given UUIDGen[IO] with
      override def randomUUID: IO[UUID] =
        IO.delay(nilUUID)

    val gateway = new RegisterTransactionGateway[IO] {
      override def register(transaction: Transaction): IO[Unit] =
        IO(assertEquals(transaction, expected, "Must transform command into domain with generated id"))
    }
    val usecase = new RegisterTransactionUseCase[IO](gateway)

    assertIO(usecase.run(command), expected, "Must receive transaction with generated id")
  }
}
