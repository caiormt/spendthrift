package spendthrift.application.modules

import cats.implicits.*

import cats.effect.*

import spendthrift.application.modules.usecases.*

object UseCases:

  def make[F[_]: Sync](repositories: Repositories[F]): F[UseCases[F]] = {
    import repositories.*

    for {
      transactionUseCase <- TransactionUseCase.make[F](transactionRepository)
    } yield new UseCases[F](
      transactionUseCase
    )
  }

end UseCases

final class UseCases[F[_]] private (
    val transactionUseCase: TransactionUseCase[F]
)
