package spendthrift.application.modules

import cats.data.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import io.chrisdavenport.epimetheus.*
import io.chrisdavenport.epimetheus.http4s.*

import natchez.*
import natchez.http4s.*

import org.http4s.*
import org.http4s.headers.*
import org.http4s.implicits.*
import org.http4s.metrics.*
import org.http4s.server.*
import org.http4s.server.middleware.*

import sup.data.*
import sup.modules.circe.given

import spendthrift.application.http.*

import spendthrift.commands.dtos.authenticateusers.*
import spendthrift.commands.usecases.user.*

import spendthrift.domain.entities.users.*
import spendthrift.domain.errors.authentication.*

import spendthrift.web.routes.healthcheck.*

import scala.concurrent.duration.*

object HttpApi:

  def make[F[_]: Async: CollectorRegistry: Trace](
      controllers: Controllers[F],
      authenticate: AuthenticateUserUseCase[F]
  ): F[HttpApi[F]] =
    EpimetheusOps
      .register[F](summon[CollectorRegistry[F]], Name("server"))
      .flatMap { metricOps =>
        Sync[F].delay(new HttpApi[F](controllers, authenticate)(metricOps))
      }

  private def classifierF[F[_]: Sync]: Request[F] => F[Option[String]] = req =>
    OptionT(TransactionRoutes.classify(req))
      .orElseF(UserRoutes.classify(req))
      .value

end HttpApi

final class HttpApi[F[_]: Async: CollectorRegistry: Trace](
    controllers: Controllers[F],
    authenticate: AuthenticateUserUseCase[F]
)(metricsOps: MetricsOps[F]):

  import HttpApi.*
  import controllers.*

  // Infrastructure Routes
  private val healthCheckRoutes = new HealthCheckRoute(healthCheckController).routes
  private val metricsRoutes     = Scraper.routes(summon[CollectorRegistry[F]])

  // Application Routes
  private val transactionRoutes = new TransactionRoutes(transactionController).authedRoutes
  private val userRoutes        = new UserRoutes(userController).authedRoutes

  // Custom Middlewares
  // format: off
  private val infrastructureMiddleware: HttpRoutes[F] => HttpRoutes[F] = {
    {
      (http: HttpRoutes[F]) => RequestLogger.httpRoutes(logHeaders = true, logBody = false)(http)
    } andThen {
      (http: HttpRoutes[F]) => ResponseLogger.httpRoutes(logHeaders = true, logBody = false)(http)
    }
  }

  private val applicationMiddleware: HttpRoutes[F] => HttpRoutes[F] = {
    {
      (http: HttpRoutes[F]) => RequestLogger.httpRoutes(logHeaders = true, logBody = true)(http)
    } andThen {
      (http: HttpRoutes[F]) => ResponseLogger.httpRoutes(logHeaders = true, logBody = true)(http)
    } andThen {
      (http: HttpRoutes[F]) => Metrics.effect[F](metricsOps, classifierF = classifierF)(http)
    } andThen {
      (http: HttpRoutes[F]) => NatchezMiddleware.server[F](http)
    }
  }
  // format: on

  // Handling authentication
  private val authUser: Kleisli[OptionT[F, *], Request[F], Principal] =
    Kleisli { req =>
      OptionT
        .fromOption[F](req.headers.get[Authorization])
        .flatMap {
          case Authorization(Credentials.Token(AuthScheme.Bearer, token)) =>
            OptionT {
              authenticate
                .run(AuthenticateUser.Jwt(token))
                .map(Some.apply)
                .recover {
                  case _: AuthenticationError => none
                }
            }

          case _: Authorization =>
            OptionT.none
        }
    }

  private val authMiddleware: AuthMiddleware[F, Principal] =
    AuthMiddleware(authUser)

  // Combining all the infrastructure routes
  private val infrastructureRoutes: HttpRoutes[F] =
    healthCheckRoutes <+> metricsRoutes

  private val applicationAuthedRoutes: AuthedRoutes[Principal, F] =
    transactionRoutes <+> userRoutes

  // Combining all the application routes
  private val applicationRoutes: HttpRoutes[F] =
    authMiddleware(applicationAuthedRoutes)

  // Combining all routes
  private val routes: HttpRoutes[F] =
    infrastructureMiddleware(infrastructureRoutes) <+> applicationMiddleware(applicationRoutes)

  // Generic Middleware
  // format: off
  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { 
      (http: HttpRoutes[F]) => AutoSlash(http)
    } andThen { 
      (http: HttpRoutes[F]) => GZip(http)
    } andThen { 
      (http: HttpRoutes[F]) => Timeout(15.seconds)(http)
    }
  }
  // format: on

  final val httpRoutes: HttpRoutes[F] = middleware(routes)

end HttpApi
