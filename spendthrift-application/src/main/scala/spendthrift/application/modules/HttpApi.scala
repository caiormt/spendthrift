package spendthrift.application.modules

import cats.data.*
import cats.implicits.*

import cats.effect.*

import org.http4s.*
import org.http4s.implicits.*
import org.http4s.server.middleware.*

import sup.data.*
import sup.modules.circe.given

import spendthrift.application.http.*

import spendthrift.web.routes.healthcheck.*

import scala.concurrent.duration.*

object HttpApi:
  def make[F[_]: Async](controllers: Controllers[F]): F[HttpApi[F]] =
    Sync[F].delay(new HttpApi[F](controllers))

final class HttpApi[F[_]: Async](controllers: Controllers[F]):

  import controllers.*

  private val healthCheckRoutes = new HealthCheckRoute(healthCheckController).routes
  private val transactionRoutes = new TransactionRoutes(transactionController).routes

  // Combining all the open http routes
  private val openRoutes: HttpRoutes[F] =
    healthCheckRoutes <+> transactionRoutes

  // Combining all routes
  private val routes: HttpRoutes[F] =
    openRoutes

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { (http: HttpRoutes[F]) =>
      AutoSlash(http)
    } andThen { (http: HttpRoutes[F]) =>
      GZip(http)
    } andThen { (http: HttpRoutes[F]) =>
      Timeout(60.seconds)(http)
    }
  }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { (http: HttpApp[F]) =>
      RequestLogger.httpApp(logHeaders = true, logBody = true)(http)
    } andThen { (http: HttpApp[F]) =>
      ResponseLogger.httpApp(logHeaders = true, logBody = true)(http)
    }
  }

  final val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

end HttpApi
