package spendthrift.web

import cats.*
import cats.implicits.*

import io.circe.*

import natchez.*

import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.*

object codec:

  final val defaultPrinter: Printer = Printer(
    dropNullValues = true,
    indent = ""
  )

  given circeEncoder[F[_], A: Encoder]: EntityEncoder[F, A] =
    jsonEncoderWithPrinterOf(defaultPrinter)

  extension [F[_]: JsonDecoder: MonadThrow: Trace](request: Request[F])
    def decodeR[A: Decoder](f: A => F[Response[F]]): F[Response[F]] = {
      val dsl = new Http4sDsl[F] {}
      import dsl._

      val attempt = Trace[F].span("codec.decodeR") {
        request.asJsonDecode[A].attempt
      }

      attempt.flatMap {
        case Right(a) => f(a)
        case Left(e)  => BadRequest()
      }
    }

end codec
