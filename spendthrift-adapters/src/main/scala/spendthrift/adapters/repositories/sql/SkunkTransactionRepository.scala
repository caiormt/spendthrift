package spendthrift.adapters.repositories.sql

import cats.implicits.*

import cats.effect.*

import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

import squants.market.*

import spendthrift.ports.*

import spendthrift.commons.*

import spendthrift.domain.entities.{ transactions => d }

import java.time.*

final class SkunkTransactionRepository[F[_]: Sync](sessionPool: Resource[F, Session[F]])
    extends RegisterTransactionGateway[F]
      with FindTransactionByIdGateway[F]:

  import SkunkTransactionSchema.*

  private val INSERT_TRANSACTION_COMMAND: Command[d.Transaction] =
    sql"INSERT INTO #$tableName(id, datetime, amount, currency, description) VALUES($codec)".command

  private val FIND_TRANSACTION_BY_ID_QUERY: Query[d.TransactionId, d.Transaction] =
    sql"SELECT id, datetime, amount, currency, description FROM #$tableName WHERE id = $id".query(codec)

  override def register(transaction: d.Transaction): F[Unit] =
    sessionPool.use { session =>
      session.prepare(INSERT_TRANSACTION_COMMAND).use(_.execute(transaction)).void
    }

  override def findById(id: d.TransactionId): F[Option[d.Transaction]] =
    sessionPool.use { session =>
      session.prepare(FIND_TRANSACTION_BY_ID_QUERY).use(_.option(id))
    }

object SkunkTransactionSchema:

  import Squants.given

  val tableName: String = "TRANSACTIONS"

  val id: Codec[d.TransactionId] =
    uuid.imap(d.TransactionId.apply)(_.value)

  val date: Codec[d.TransactionDate] = {
    def offsetDateTimeToTransactionDateUTC(odt: OffsetDateTime): d.TransactionDate =
      d.TransactionDate(odt.atZoneSameInstant(ZoneOffset.UTC))

    def transactionDateToOffsetDateTimeUTC(date: d.TransactionDate): OffsetDateTime =
      date.value.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime

    timestamptz(6).imap(offsetDateTimeToTransactionDateUTC)(transactionDateToOffsetDateTimeUTC)
  }

  val amount: Codec[d.TransactionValue] = {
    type ErrorMessage = String

    def currencyToTransactionValue(amount: BigDecimal, code: String): Either[ErrorMessage, d.TransactionValue] =
      Currency(code).toEither.bimap(_.getMessage, Money(amount, _)).map(d.TransactionValue.apply)

    (numeric(13, 4) ~ bpchar(3)).eimap {
      case amount ~ code => currencyToTransactionValue(amount, code)
    }(value => value.value.amount ~ value.value.currency.code)
  }

  val description: Codec[d.TransactionDescription] =
    varchar(255).imap(d.TransactionDescription.apply)(_.value)

  val codec: Codec[d.Transaction] =
    (id ~ date ~ amount ~ description).gimap[d.Transaction]

end SkunkTransactionSchema
