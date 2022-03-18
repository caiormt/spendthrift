package spendthrift.application.modules.usecases

import cats.implicits.*

import cats.effect.*

import spendthrift.application.modules.*
import spendthrift.application.modules.repositories.*

import spendthrift.commands.usecases.transaction.*

import spendthrift.queries.usecases.transaction.*

object TransactionUseCase:

  def make[F[_]: Sync](repositories: TransactionRepository[F]): F[TransactionUseCase[F]] = {
    import repositories.*

    for {
      registerTransactionUseCase <- RegisterTransactionUseCase.make[F](registerTransactionGateway)
      findTransactionByIdUseCase <- FindTransactionByIdUseCase.make[F](findTransactionByIdGateway)
    } yield new TransactionUseCase[F](
      registerTransactionUseCase,
      findTransactionByIdUseCase
    )
  }

end TransactionUseCase

final class TransactionUseCase[F[_]] private (
    val registerTransactionUseCase: RegisterTransactionUseCase[F],
    val findTransactionByIdUseCase: FindTransactionByIdUseCase[F]
)
