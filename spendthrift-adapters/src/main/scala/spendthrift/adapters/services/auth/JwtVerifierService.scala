package spendthrift.adapters.services.auth

import cats.implicits.*

import cats.effect.{ Trace => _, * }

import natchez.*

import com.auth0.jwt.*
import com.auth0.jwt.algorithms.*
import com.auth0.jwt.exceptions.*
import com.auth0.jwt.impl.PublicClaims.*

import spendthrift.ports.*

import spendthrift.domain.entities.users.*
import spendthrift.domain.errors.authentication.*

import java.util.*

object JwtVerifierService:

  def make[F[_]: Sync: Trace](secret: Array[Byte]): F[JwtVerifierService[F]] =
    algorithm[F](secret).flatMap(verifier[F]).flatMap { verifier =>
      Sync[F].delay(new JwtVerifierService[F](verifier))
    }

  def algorithm[F[_]: Sync](secret: Array[Byte]): F[Algorithm] =
    Sync[F].delay(Algorithm.HMAC512(secret))

  def verifier[F[_]: Sync](algorithm: Algorithm): F[JWTVerifier] =
    Sync[F].delay {
      JWT
        .require(algorithm)
        .withIssuer("spendthrift")
        .withClaimPresence(SUBJECT)
        .build
    }

end JwtVerifierService

final class JwtVerifierService[F[_]: Sync: Trace](verifier: JWTVerifier) extends AuthenticateUserJwtGateway[F]:

  override def authenticate(token: String): F[Principal] =
    Trace[F].span("service.jwt-verifier.authenticate") {
      verify(token).flatMap { jwt =>
        for {
          userId <- userId(jwt.getSubject)
        } yield UserPrincipal(
          userId
        )
      }
    }

  private def verify(token: String): F[interfaces.DecodedJWT] =
    Trace[F].span("service.jwt-verifier.verify") {
      Sync[F].delay(verifier.verify(token)).adaptError {
        case t: JWTDecodeException =>
          MalformedJwtToken(t.getMessage)

        case t @ (_: AlgorithmMismatchException | _: SignatureVerificationException | _: InvalidClaimException) =>
          InvalidJwtToken(t.getMessage)

        case _: TokenExpiredException =>
          ExpiredJwtToken
      }
    }

  private def userId(subject: String): F[UserId] =
    Trace[F].span("service.jwt-verifier.extract-user") {
      Sync[F].delay(UUID.fromString(subject)).map(UserId.apply).adaptError {
        case e: IllegalArgumentException =>
          MalformedJwtToken(s"The Claim '$SUBJECT' value doesn't match the required format: ${e.getMessage}")
      }
    }
