package spendthrift.application

import cats.data.*
import cats.implicits.*

import cats.effect.std.*
import cats.effect.{ Trace => _, * }

import com.comcast.ip4s.*

import fs2.io.net.*

import io.jaegertracing.Configuration.*

import natchez.*
import natchez.http4s.implicits.*
import natchez.jaeger.*

import org.http4s.*
import org.http4s.ember.server.*
import org.http4s.server.*

import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.*

import sup.*
import sup.data.*

import spendthrift.effect.extensions.sup.*

import spendthrift.adapters.repositories.sql.*

import spendthrift.application.config.data.*
import spendthrift.application.modules.*

import java.net.*

import scala.concurrent.duration.*

object App extends IOApp.Simple:

  override def run: IO[Unit] =
    application[IO].as(ExitCode.Success)

  def application[F[_]: Async: Network: Console]: F[Unit] =
    config[F].flatMap { config =>
      entryPoint[F]
        .flatMap { entryPoint =>
          type G[A] = Kleisli[F, Span[F], A]

          val httpRoutes =
            Resources
              .make[G](config)
              .evalMap(api[G])
              .map(_.httpRoutes)

          entryPoint
            .liftR(httpRoutes)
            .map(_.orNotFound)
            .flatMap(server[F](config.http))
        }
        .useForever
        .void
    }

  def config[F[_]: Async]: F[AppConfig] =
    Slf4jLogger.create[F].flatMap { logger =>
      appConfig.load[F].flatTap(config => logger.info(show"Loaded configurations: $config"))
    }

  def server[F[_]: Async](config: HttpConfig)(httpApp: HttpApp[F]): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHttp2
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(httpApp)
      .build

  def api[F[_]: Async: Network: Console: Trace](resources: Resources[F]): F[HttpApi[F]] = {
    import resources.given

    for {
      repositories   <- Repositories.makeSkunk[F](resources.sessionPool)
      healthReporter <- healthReporter[F](resources)
      useCases       <- UseCases.make[F](repositories)
      controllers    <- Controllers.make[F](useCases, healthReporter)
      api            <- HttpApi.make[F](controllers)
    } yield api
  }

  def healthReporter[F[_]: Async](resources: Resources[F]): F[HealthReporter[F, NonEmptyList, Tagged[String, *]]] = {
    import resources.*

    for {
      repository     <- SkunkHealthCheck.make[F](sessionPool)
      healthReporter <- Sync[F].delay {
                          HealthReporter.fromChecks(
                            repository.healthcheck.through(
                              cached("repository", 10.seconds.some)(cache.healthCheck)
                            )
                          )
                        }
    } yield healthReporter
  }

  def entryPoint[F[_]: Sync]: Resource[F, EntryPoint[F]] =
    Jaeger.entryPoint[F](system = "spendthrift") { config =>
      Sync[F].blocking {
        config
          .withSampler(SamplerConfiguration.fromEnv)
          .withReporter(ReporterConfiguration.fromEnv)
          .getTracer
      }
    }

end App
