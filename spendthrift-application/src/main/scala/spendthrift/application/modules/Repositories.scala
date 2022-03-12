package spendthrift.application.modules

import cats.implicits.*

import cats.effect.*

import spendthrift.application.modules.repositories.*

object Repositories:
  def make[F[_]: Sync]: F[Repositories[F]] =
    for {
      transactionRepository <- TransactionRepository.make[F]
    } yield new Repositories[F](
      transactionRepository
    )

final class Repositories[F[_]] private (
    val transactionRepository: TransactionRepository[F]
)
