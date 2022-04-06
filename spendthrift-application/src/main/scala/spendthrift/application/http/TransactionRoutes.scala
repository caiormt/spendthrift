package spendthrift.application.http

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import org.http4s.*

import spendthrift.application.modules.controllers.*

import spendthrift.domain.entities.users.*

import spendthrift.web.routes.transaction.*

object TransactionRoutes:

  private val RESOURCE_PATH_R =
    "/transactions/?".r

  private val RESOURCE_ID_PATH_R =
    "/transactions/[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}/?".r

  def classify[F[_]: Sync](request: Request[F]): F[Option[String]] =
    Sync[F].blocking {
      request.uri.renderString match {
        case RESOURCE_PATH_R()    => "/transactions".some
        case RESOURCE_ID_PATH_R() => "/transactions/{transaction_id}".some
        case _                    => none
      }
    }

end TransactionRoutes

final class TransactionRoutes[F[_]: Concurrent: Trace](controllers: TransactionController[F]):

  import controllers.*

  private val registerTransactionRoute =
    new RegisterTransactionRoute[F](registerTransactionController).routes

  private val findTransactionByIdRoute =
    new FindTransactionByIdRoute[F](findTransactionByIdController).routes

  final val authedRoutes: AuthedRoutes[Principal, F] =
    registerTransactionRoute <+> findTransactionByIdRoute

end TransactionRoutes
