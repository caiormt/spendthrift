package spendthrift.web.routes.transaction

import cats.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.*
import org.http4s.headers.*
import org.http4s.implicits.*

import spendthrift.domain.entities.users.*

import spendthrift.presentation.controllers.transaction.*
import spendthrift.presentation.views.findtransactionsbyid.*

import spendthrift.web.codec.given

final class FindTransactionByIdRoute[F[_]: MonadThrow: Trace](controller: FindTransactionByIdController[F])
    extends Http4sDsl[F]:

  final val routes: AuthedRoutes[Principal, F] = AuthedRoutes.of[Principal, F] {
    case GET -> Root / "transactions" / UUIDVar(id) as user =>
      Trace[F].span("routes.find-transaction-by-id") {
        controller.run(FindTransactionById(id)).flatMap {
          case Some(transaction) => Ok(transaction)
          case None              => NotFound()
        }
      }
  }
