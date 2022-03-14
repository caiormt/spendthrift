package spendthrift.application

import cats.implicits.*

import cats.effect.*
import cats.effect.std.*

import fs2.io.net.*

import org.http4s.ember.server.*
import org.http4s.server.*

import com.comcast.ip4s.*

import spendthrift.application.modules.*

object App extends IOApp.Simple:

  override def run: IO[Unit] =
    application[IO].as(ExitCode.Success)

  def application[F[_]: Async: Network: Console]: F[Unit] =
    Resources
      .make[F]
      .evalMap(api[F])
      .flatMap(server[F])
      .useForever
      .void

  def server[F[_]: Async](api: HttpApi[F]): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHttp2
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8081")
      .withHttpApp(api.httpApp)
      .build

  def api[F[_]: Async: Network: Console](resources: Resources[F]): F[HttpApi[F]] =
    for {
      repositories <- Repositories.makeSkunk[F](resources.sessionPool)
      useCases     <- UseCases.make[F](repositories)
      controllers  <- Controllers.make[F](useCases)
      api          <- HttpApi.make[F](controllers)
    } yield api

end App
