package spendthrift.application.http

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import org.http4s.*

import spendthrift.application.modules.controllers.*

import spendthrift.web.routes.transaction.*

object TransactionRoutes:

  private val RESOURCE_PATH_R = "^/transactions".r.unanchored

  def classify(renderedUri: String): Option[String] =
    renderedUri match {
      case RESOURCE_PATH_R() => "/transactions".some
      case _                 => none
    }

end TransactionRoutes

final class TransactionRoutes[F[_]: Concurrent: Trace](controllers: TransactionController[F]):

  import controllers.*

  private val registerTransactionRoute =
    new RegisterTransactionRoute[F](registerTransactionController).routes

  private val findTransactionByIdRoute =
    new FindTransactionByIdRoute[F](findTransactionByIdController).routes

  final val routes: HttpRoutes[F] =
    registerTransactionRoute <+> findTransactionByIdRoute

end TransactionRoutes
