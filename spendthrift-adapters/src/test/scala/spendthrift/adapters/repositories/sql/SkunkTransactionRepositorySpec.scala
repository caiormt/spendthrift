package spendthrift.adapters.repositories.sql

import cats.implicits.*
import cats.effect.*

import munit.*

import spendthrift.domain.entities.{ transactions => d }

import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import java.time.*
import squants.market.*

final class SkunkTransactionRepositorySpec extends SqlSuite {

  override val cleanUpTables: List[String] =
    List(SkunkTransactionSchema.tableName)

  test("should persist new transaction converting timestamp to UTC") {
    val txId          = new java.util.UUID(0L, 0L)
    val txDate        = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
    val txAmount      = BigDecimal(7.50)
    val txCurrency    = BRL
    val txDescription = "Gasoline"

    val transaction = d.Transaction(
      d.TransactionId(txId),
      d.TransactionDate(txDate),
      d.TransactionValue(Money(txAmount, txCurrency)),
      d.TransactionDescription(txDescription)
    )

    val repository = new SkunkTransactionRepository[IO](sessionPool)
    val register   = repository.register(transaction)

    val sql    = sql"SELECT id, datetime, amount, currency, description FROM TRANSACTIONS WHERE id = $uuid"
    val query  = sql.query(uuid ~ timestamptz(6) ~ numeric(13, 4) ~ bpchar(3) ~ varchar(255))
    val result = session.prepare(query).use(_.option(txId))

    register *> result.map {
      case None                                                  =>
        fail("Should retrieve persisted transaction")
      case Some(id ~ datetime ~ amount ~ currency ~ description) =>
        assertEquals(id, txId)
        assertEquals(datetime, txDate.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime)
        assertEquals(amount, txAmount)
        assertEquals(currency, txCurrency.code)
        assertEquals(description, txDescription)
    }
  }

  test("should return empty when transaction id not found") {
    val repository = new SkunkTransactionRepository[IO](sessionPool)
    val result     = repository.findById(d.TransactionId(java.util.UUID.randomUUID))

    result.map(assertEquals(_, none, "Must receive empty response when not found"))
  }

  test("should return transaction found by id") {
    val txId          = new java.util.UUID(0L, 0L)
    val txDate        = ZonedDateTime.now(ZoneOffset.UTC)
    val date          = txDate.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime
    val txAmount      = BigDecimal(7.50)
    val txCurrency    = BRL
    val txDescription = "Gasoline"

    val expected = d.Transaction(
      d.TransactionId(txId),
      d.TransactionDate(txDate),
      d.TransactionValue(Money(txAmount, txCurrency)),
      d.TransactionDescription(txDescription)
    )

    val codec    = uuid ~ timestamptz(6) ~ numeric(13, 4) ~ bpchar(3) ~ varchar(255)
    val sql      = sql"INSERT INTO TRANSACTIONS VALUES ($codec)".command
    val register = session.prepare(sql).use(_.execute(txId ~ date ~ txAmount ~ txCurrency.code ~ txDescription))

    val repository = new SkunkTransactionRepository[IO](sessionPool)
    val result     = repository.findById(d.TransactionId(txId))

    register *> result.map {
      case None              =>
        fail("Should retrieve persisted transaction")
      case Some(transaction) =>
        assertEquals(transaction, expected)
    }
  }
}
