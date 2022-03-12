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
      registerTransactionUseCase <- Sync[F].delay(new RegisterTransactionUseCase[F](registerTransactionGateway))
      findTransactionByIdUseCase <- Sync[F].delay(new FindTransactionByIdUseCase[F](findTransactionByIdGateway))
    } yield new TransactionUseCase[F](
      registerTransactionUseCase,
      findTransactionByIdUseCase
    )
  }

final class TransactionUseCase[F[_]] private (
    val registerTransactionUseCase: RegisterTransactionUseCase[F],
    val findTransactionByIdUseCase: FindTransactionByIdUseCase[F]
)
