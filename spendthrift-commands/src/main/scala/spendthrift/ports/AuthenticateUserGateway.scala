package spendthrift.ports

import spendthrift.domain.entities.users.*

sealed trait AuthenticateUserGateway[F[_], A]:
  def authenticate(input: A): F[Principal]

trait AuthenticateUserJwtGateway[F[_]] extends AuthenticateUserGateway[F, String]:
  def authenticate(token: String): F[Principal]
