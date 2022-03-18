package spendthrift.web.routes.healthcheck

import cats.*
import cats.data.*
import cats.implicits.*

import cats.effect.*

import io.circe.*

import org.http4s.*
import org.http4s.dsl.*

import sup.*
import sup.data.*

import spendthrift.presentation.controllers.healthcheck.*

import spendthrift.web.codec.given

final class HealthCheckRoute[F[_]: Monad, G[_]: NonEmptyTraverse, H[_]: Reducible](
    controller: HealthCheckController[F, G, H]
)(using encoder: Encoder[Report[G, H, Health]])
    extends Http4sDsl[F]:

  final val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "health" =>
      controller.run.flatMap { report =>
        report.reduce match {
          case Health.Healthy => Ok(report)
          case Health.Sick    => ServiceUnavailable(report)
        }
      }
  }
