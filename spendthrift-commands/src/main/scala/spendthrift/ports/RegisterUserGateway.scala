package spendthrift.ports

import spendthrift.domain.entities.users.*

trait RegisterUserGateway[F[_]]:
  def register(user: User): F[Unit]
