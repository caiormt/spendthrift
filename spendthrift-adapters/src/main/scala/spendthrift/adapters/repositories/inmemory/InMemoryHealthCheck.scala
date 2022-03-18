package spendthrift.adapters.repositories.inmemory

import cats.*
import cats.implicits.*

import cats.effect.*

import sup.*
import sup.data.*
import sup.mods.*

object InMemoryHealthCheck:
  def make[F[_]: Sync]: F[InMemoryHealthCheck[F]] =
    Sync[F].delay(new InMemoryHealthCheck[F])

final class InMemoryHealthCheck[F[_]: Applicative]:

  def healthcheck: HealthCheck[F, Tagged[String, *]] =
    HealthCheck.const[F, Id](Health.Healthy).through(tagWith("repository"))
