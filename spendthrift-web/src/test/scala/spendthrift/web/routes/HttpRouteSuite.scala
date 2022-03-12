package spendthrift.web.routes

import cats.implicits.*

import cats.effect.*

import io.circe.*

import org.http4s.*
import org.http4s.circe.*

import munit.*

abstract class HttpRouteSuite extends CatsEffectSuite {

  final type ResponseCheck = Response[IO] => IO[Unit]

  def check[A](
      response: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[A] = None,
      f: ResponseCheck = _ => IO.unit
  )(using loc: Location, ev: EntityDecoder[IO, A]): IO[Unit] =
    checkAll[A](response, expectedStatus, expectedBody.some, f)

  def checkAll[A](
      response: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[Option[A]] = None,
      f: ResponseCheck = _ => IO.unit
  )(using loc: Location, ev: EntityDecoder[IO, A]): IO[Unit] =
    response.flatMap { response =>
      def checkContentBody(expected: A) = assertIO(response.as[A], expected, clue = "body not expected")
      val checkEmptyBody                = assertIOBoolean(response.body.compile.last.map(_.isEmpty), clue = "no body was expected")
      val checkStatusCode               = assertIO(IO(response.status), expectedStatus, clue = "status code not expected")
      val checkBody                     = expectedBody.fold(IO.unit)(_.fold(checkEmptyBody)(checkContentBody))
      checkStatusCode *> checkBody *> f(response)
    }

  def checkLocationHeader(context: Uri, fieldName: String = "id")(using loc: Location): ResponseCheck = response =>
    for {
      cursor   <- response.asJson.map(_.hcursor)
      id       <- cursor.downField(fieldName).as[String].liftTo[IO]
      location <- response.headers.get[headers.Location].liftTo[IO](new IllegalStateException("Location not found"))
    } yield assertEquals(location.uri, context / id, clue = "location not expected")

  def checkResponseBody(expected: Json, fieldName: String = "id")(using loc: Location): ResponseCheck =
    _.asJson.flatMap { json =>
      for {
        id <- json.hcursor.downField(fieldName).as[Json].liftTo[IO]
      } yield assertEquals(json, expected.deepMerge(Json.obj(fieldName -> id)), clue = "body not expected")
    }
}
