package spendthrift.web.routes.transaction

import cats.*
import cats.implicits.*

import cats.effect.*

import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.*
import org.http4s.headers.*
import org.http4s.implicits.*

import spendthrift.presentation.controllers.transaction.*
import spendthrift.presentation.views.findtransactionsbyid.*

import spendthrift.web.codec.{ *, given }

final class FindTransactionByIdRoute[F[_]: MonadThrow](controller: FindTransactionByIdController[F])
    extends Http4sDsl[F]:

  final val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "transactions" / UUIDVar(id) =>
      controller.run(FindTransactionById(id)).flatMap {
        case Some(transaction) => Ok(transaction)
        case None              => NotFound()
      }
  }
