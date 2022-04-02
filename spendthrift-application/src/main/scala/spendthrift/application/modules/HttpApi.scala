package spendthrift.application.modules

import cats.data.*
import cats.implicits.*

import cats.effect.{ Trace => _, * }

import io.chrisdavenport.epimetheus.*
import io.chrisdavenport.epimetheus.http4s.*

import natchez.*
import natchez.http4s.*

import org.http4s.*
import org.http4s.implicits.*
import org.http4s.metrics.*
import org.http4s.server.middleware.*

import sup.data.*
import sup.modules.circe.given

import spendthrift.application.http.*

import spendthrift.web.routes.healthcheck.*

import scala.concurrent.duration.*

object HttpApi:

  def make[F[_]: Async: CollectorRegistry: Trace](controllers: Controllers[F]): F[HttpApi[F]] =
    EpimetheusOps
      .register[F](summon[CollectorRegistry[F]], Name("server"))
      .map(meteredRoutes[F])
      .flatMap { meteredRoutes =>
        Sync[F].delay(new HttpApi[F](controllers)(meteredRoutes))
      }

  private def meteredRoutes[F[_]: Sync](metricOps: MetricsOps[F]): HttpRoutes[F] => HttpRoutes[F] =
    Metrics.effect[F](metricOps, classifierF = req => classifiers(req.uri.renderString))

  private def classifiers[F[_]: Sync](renderedUri: String): F[Option[String]] =
    Sync[F].blocking {
      // format: off
      TransactionRoutes.classify(renderedUri) orElse
      UserRoutes.classify(renderedUri)
      // format: on
    }

end HttpApi

final class HttpApi[F[_]: Async: CollectorRegistry: Trace](controllers: Controllers[F])(
    meteredRoutes: HttpRoutes[F] => HttpRoutes[F]
):

  import controllers.*

  // Infrastructure Routes
  private val healthCheckRoutes = new HealthCheckRoute(healthCheckController).routes
  private val metricsRoutes     = Scraper.routes(summon[CollectorRegistry[F]])

  // Application Routes
  private val transactionRoutes = new TransactionRoutes(transactionController).routes
  private val userRoutes        = new UserRoutes(userController).routes

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
      (http: HttpRoutes[F]) => meteredRoutes(http)
    } andThen {
      (http: HttpRoutes[F]) => NatchezMiddleware.server[F](http)
    }
  }
  // format: on

  // Combining all the infrastructure routes
  private val infrastructureRoutes: HttpRoutes[F] =
    healthCheckRoutes <+> metricsRoutes

  // Combining all the application routes
  private val applicationRoutes: HttpRoutes[F] =
    transactionRoutes <+> userRoutes

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
