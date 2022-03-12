package spendthrift.application.http

import cats.implicits.*

import cats.effect.*

import org.http4s.*

import spendthrift.application.modules.controllers.*

import spendthrift.web.routes.transaction.*

final class TransactionRoutes[F[_]: Concurrent](controllers: TransactionController[F]):

  import controllers.*

  private val registerTransactionRoute =
    new RegisterTransactionRoute[F](registerTransactionController).routes

  private val findTransactionByIdRoute =
    new FindTransactionByIdRoute[F](findTransactionByIdController).routes

  final val routes: HttpRoutes[F] =
    registerTransactionRoute <+> findTransactionByIdRoute

end TransactionRoutes
