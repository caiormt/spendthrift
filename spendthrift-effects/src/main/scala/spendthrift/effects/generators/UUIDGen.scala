package spendthrift.effects.generators

import cats.effect.*

import java.util.*

trait UUIDGen[F[_]]:
  def randomUUID: F[UUID]

object UUIDGen:

  given syncUUIDGen[F[_]: Sync]: UUIDGen[F] with
    override def randomUUID: F[UUID] =
      Sync[F].delay(UUID.randomUUID)

  def apply[F[_]](using ev: UUIDGen[F]): UUIDGen[F] = ev

  def randomUUID[F[_]: UUIDGen]: F[UUID] =
    UUIDGen[F].randomUUID
