package spendthrift.application.modules

import cats.implicits.*

import cats.effect.*
import cats.effect.std.*

import fs2.io.net.*

import natchez.Trace.Implicits.noop

import skunk.*
import skunk.implicits.*
import skunk.util.*

object Resources:
  def make[F[_]: Concurrent: Network: Console]: Resource[F, Resources[F]] = {
    def makeDatabase: SessionPool[F] =
      Session.pooled(
        host = "127.0.0.1",
        user = "spendthrift",
        database = "spendthrift",
        password = "spendthrift@dev".some,
        max = 3
      )

    for {
      sessionPool <- makeDatabase
    } yield new Resources[F](
      sessionPool
    )
  }

final class Resources[F[_]] private (
    val sessionPool: Resource[F, Session[F]]
)
