package spendthrift.application.modules

import cats.implicits.*

import cats.effect.*

import skunk.*

import spendthrift.application.modules.repositories.*

object Repositories:

  def makeInMemory[F[_]: Sync]: F[Repositories[F]] =
    for {
      transactionRepository <- TransactionRepository.makeInMemory[F]
    } yield new Repositories[F](
      transactionRepository
    )

  def makeSkunk[F[_]: Sync](sessionPool: Resource[F, Session[F]]): F[Repositories[F]] =
    for {
      transactionRepository <- TransactionRepository.makeSkunk[F](sessionPool)
    } yield new Repositories[F](
      transactionRepository
    )

end Repositories

final class Repositories[F[_]] private (
    val transactionRepository: TransactionRepository[F]
)
