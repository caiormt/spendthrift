package spendthrift.presentation.views

import cats.implicits.*

import io.circe.*

import squants.*
import squants.market.*

import spendthrift.commons.Squants.given

given decoderSquantsCurresncy: Decoder[Currency] =
  Decoder.decodeString.emapTry(Currency.apply)

given decoderSquantsMoney: Decoder[Money] =
  decoderSquantsMoneyDefault.or(decoderSquantsMoneyObject)

given encoderSquantsMoney: Encoder[Money] with
  override def apply(a: Money): Json =
    Json.obj(
      ("amount"   -> Json.fromBigDecimal(a.amount)),
      ("currency" -> Json.fromString(a.currency.code))
    )

final val decoderSquantsMoneyDefault: Decoder[Money] =
  Decoder.decodeBigDecimal.map(Money(_, BRL))

final val decoderSquantsMoneyObject: Decoder[Money] = new Decoder[Money] {
  override def apply(c: HCursor): Decoder.Result[Money] =
    for {
      amount   <- c.downField("amount").as[BigDecimal]
      currency <- c.getOrElse[Currency]("currency")(BRL)
    } yield Money(amount, currency)
}
