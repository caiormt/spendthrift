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

import spendthrift.presentation.controllers.transaction.*
import spendthrift.presentation.views.registertransactions.*

import spendthrift.web.codec.{ *, given }

final class RegisterTransactionRoute[F[_]: MonadThrow: JsonDecoder: Trace](controller: RegisterTransactionController[F])
    extends Http4sDsl[F]:

  final val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "transactions" =>
      Trace[F].span("routes.register-transaction") {
        request.decodeR[RegisterTransaction] { view =>
          controller.run(view).flatMap { transaction =>
            Created(transaction, Location(uri"/transactions" / transaction.id.show))
          }
        }
      }
  }
