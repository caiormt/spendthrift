package spendthrift.commons

import cats.*

import squants.*
import squants.market.*

object Squants:

  object PYG extends Currency("PYG", "Paraguayan guaraní", "₲", 0)

  final val defaultCurrencySet =
    Set(BRL, USD, EUR, PYG)

  given defaultMoneyContext: MoneyContext =
    MoneyContext(defaultCurrency = BRL, defaultCurrencySet, Nil)

  given eqSquants[A <: Quantity[A]]: Eq[A] with
    override def eqv(x: A, y: A): Boolean = x.equals(y)

  given orderSquants[A <: Quantity[A]]: Order[A] with
    override def compare(x: A, y: A): Int = x.compareTo(y)

  given showSquants[A <: Quantity[A]]: Show[A] with
    override def show(t: A): String = t.toString

end Squants
