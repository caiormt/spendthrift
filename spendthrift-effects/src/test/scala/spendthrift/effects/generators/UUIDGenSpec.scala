package spendthrift.effects.generators

import cats.implicits.*

import cats.effect.*

import munit.*

final class UUIDGenSpec extends CatsEffectSuite {

  test("Instance must generate random UUID v4") {
    val gen    = summon[UUIDGen[IO]]
    val random = gen.randomUUID
    assertIOBoolean((random, random).tupled.map(_ =!= _))
  }

  test("Object must generate random UUID v4") {
    val random = UUIDGen.randomUUID[IO]
    assertIOBoolean((random, random).tupled.map(_ =!= _))
  }
}
