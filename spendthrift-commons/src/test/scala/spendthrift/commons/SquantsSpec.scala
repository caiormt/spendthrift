package spendthrift.commons

import cats.*
import cats.implicits.*

import squants.*
import squants.market.*

import munit.*

import spendthrift.commons.*

final class SquantsSpec extends CatsEffectSuite {

  import Squants.{ *, given }

  test("Money Context should have BRL as default currency") {
    val defaultMoneyContext = summon[MoneyContext]
    assertEquals(defaultMoneyContext.defaultCurrency, BRL)
  }

  test("Money Context should allow only known currencies") {
    val defaultMoneyContext = summon[MoneyContext]
    assertEquals(defaultMoneyContext.currencies, Set(BRL, USD, EUR, PYG))
  }

  test("Money Context should not have any default conversions") {
    val defaultMoneyContext = summon[MoneyContext]
    assertEquals(defaultMoneyContext.rates, Nil)
  }

  test("Cats.Eq[Quantity] should delegate to Quantity.equals()") {
    assert(BRL(1) === BRL(1))
  }

  test("Cats.Order[Quantity] should delegate to Quantity.compareTo()") {
    val order = summon[Order[Money]]
    assertEquals(order.compare(BRL(1), BRL(2)), BRL(1).compareTo(BRL(2)))
  }

  test("Cats.Show[Quantity] should delete to Quantity.toString()") {
    assertEquals(BRL(1).toString, BRL(1).show)
  }
}
