package spendthrift.queries.usecases.transaction

import cats.implicits.*

import cats.effect.*

import natchez.Trace.Implicits.noop

import squants.market.*

import munit.*

import spendthrift.ports.*

import spendthrift.domain.entities.transactions.*

import spendthrift.queries.dtos.findtransactionsbyid.*

import java.util.*

final class FindTransactionByIdUseCaseSpec extends CatsEffectSuite {

  test("Must call gateway and proxy empty response") {
    val transactionId = UUID.randomUUID

    val gateway = new FindTransactionByIdGateway[IO] {
      override def findById(id: TransactionId): IO[Option[Transaction]] =
        IO(assertEquals(id.value, transactionId, "Must find by received id")) *> IO.none
    }
    val usecase = new FindTransactionByIdUseCase[IO](gateway)

    assertIO(usecase.run(FindTransactionById(transactionId)), none, "Must return result from gateway")
  }

  test("Must call gateway and proxy response") {
    val transactionId = UUID.randomUUID

    val transaction = Transaction(
      TransactionId(transactionId),
      TransactionDate.now(),
      TransactionValue(7.50),
      TransactionDescription("Gasoline")
    )

    val gateway = new FindTransactionByIdGateway[IO] {
      override def findById(id: TransactionId): IO[Option[Transaction]] =
        IO(assertEquals(id.value, transactionId, "Must find by received id")) *> IO.delay(transaction.some)
    }
    val usecase = new FindTransactionByIdUseCase[IO](gateway)

    assertIO(usecase.run(FindTransactionById(transactionId)), transaction.some, "Must return result from gateway")
  }
}
