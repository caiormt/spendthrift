package spendthrift.adapters.services.auth

import cats.implicits.*

import cats.effect.*

import io.circe.*

import natchez.Trace.Implicits.noop

import com.auth0.jwt.algorithms.*
import munit.*
import scodec.*
import scodec.bits.*

import spendthrift.domain.errors.authentication.*

import java.nio.charset.StandardCharsets.*
import java.util.*

final class JwtVerifierServiceSpec extends CatsEffectSuite {

  private[this] var _algorithm: Algorithm             = _
  private[this] var _verifier: JwtVerifierService[IO] = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    val secret = "secret".getBytes(UTF_8)

    val (algorithm, verifier) = {
      val algorithm = JwtVerifierService.algorithm[IO](secret)
      val verifier  = JwtVerifierService.make[IO](secret)
      (algorithm, verifier).tupled.unsafeRunSync()
    }

    _algorithm = algorithm
    _verifier = verifier
  }

  test("should fail with malformed token") {
    val nilUUID = new UUID(0L, 0L)

    val headerJson = Json.obj()
    val header     = json2Base64(headerJson)

    val payloadJson = Json.obj(
      "iss" -> Json.fromString("spendthrift"),
      "sub" -> Json.fromString(nilUUID.toString)
    )
    val payload     = json2Base64(payloadJson)

    interceptIO[MalformedJwtToken](_verifier.authenticate(s"$header.")) *>
      interceptIO[MalformedJwtToken](_verifier.authenticate(s"$header.$payload")) *>
      interceptIO[MalformedJwtToken](_verifier.authenticate(s"$header..")) *>
      interceptIO[MalformedJwtToken](_verifier.authenticate(s"$header..$payload"))
  }

  test("should fail with invalid algorithm") {
    val nilUUID = new UUID(0L, 0L)

    val headerJson = Json.obj(
      "alg" -> Json.fromString("HS156"),
      "typ" -> Json.fromString("JWT")
    )
    val header     = json2Base64(headerJson)

    val payloadJson = Json.obj(
      "iss" -> Json.fromString("spendthrift"),
      "sub" -> Json.fromString(nilUUID.toString)
    )
    val payload     = json2Base64(payloadJson)

    val signature = sign(header, payload)

    interceptMessageIO[InvalidJwtToken]("The provided Algorithm doesn't match the one defined in the JWT's Header.") {
      _verifier.authenticate(s"$header.$payload.$signature")
    }
  }

  test("should fail with wrong algorithm") {
    val nilUUID = new UUID(0L, 0L)

    val headerJson = Json.obj(
      "alg" -> Json.fromString("HS256"),
      "typ" -> Json.fromString("JWT")
    )
    val header     = json2Base64(headerJson)

    val payloadJson = Json.obj(
      "iss" -> Json.fromString("spendthrift"),
      "sub" -> Json.fromString(nilUUID.toString)
    )
    val payload     = json2Base64(payloadJson)

    val signature = sign(header, payload)

    interceptMessageIO[InvalidJwtToken]("The provided Algorithm doesn't match the one defined in the JWT's Header.") {
      _verifier.authenticate(s"$header.$payload.$signature")
    }
  }

  test("should fail with wrong issuer") {
    val nilUUID = new UUID(0L, 0L)

    val headerJson = Json.obj(
      "alg" -> Json.fromString("HS512"),
      "typ" -> Json.fromString("JWT")
    )
    val header     = json2Base64(headerJson)

    val payloadJson = Json.obj(
      "iss" -> Json.fromString("thrift"),
      "sub" -> Json.fromString(nilUUID.toString)
    )
    val payload     = json2Base64(payloadJson)

    val signature = sign(header, payload)

    interceptMessageIO[InvalidJwtToken]("The Claim 'iss' value doesn't match the required issuer.") {
      _verifier.authenticate(s"$header.$payload.$signature")
    }
  }

  test("should fail without subject") {
    val nilUUID = new UUID(0L, 0L)

    val headerJson = Json.obj(
      "alg" -> Json.fromString("HS512"),
      "typ" -> Json.fromString("JWT")
    )
    val header     = json2Base64(headerJson)

    val payloadJson = Json.obj(
      "iss" -> Json.fromString("spendthrift")
    )
    val payload     = json2Base64(payloadJson)

    val signature = sign(header, payload)

    interceptMessageIO[InvalidJwtToken]("The Claim 'sub' is not present in the JWT.") {
      _verifier.authenticate(s"$header.$payload.$signature")
    }
  }

  test("should fail with malformed subject") {
    val id = "nilUUID"

    val headerJson = Json.obj(
      "alg" -> Json.fromString("HS512"),
      "typ" -> Json.fromString("JWT")
    )
    val header     = json2Base64(headerJson)

    val payloadJson = Json.obj(
      "iss" -> Json.fromString("spendthrift"),
      "sub" -> Json.fromString(id)
    )
    val payload     = json2Base64(payloadJson)

    val signature = sign(header, payload)

    val message = s"The Claim 'sub' value doesn't match the required format: Invalid UUID string: $id"
    interceptMessageIO[MalformedJwtToken](message) {
      _verifier.authenticate(s"$header.$payload.$signature")
    }
  }

  private def json2Base64(json: Json): String =
    base64(json.noSpaces.getBytes(UTF_8))

  private def sign(header: String, payload: String): String =
    base64(_algorithm.sign(header.getBytes(UTF_8), payload.getBytes(UTF_8)))

  private def base64(bytes: Array[Byte]): String =
    ByteVector(bytes).toBase64UrlNoPad
}
