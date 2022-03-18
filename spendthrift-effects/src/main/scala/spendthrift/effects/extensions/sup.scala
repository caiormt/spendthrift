package spendthrift.effect.extensions

import cats.*
import cats.implicits.*

import cats.effect.*

import io.chrisdavenport.mules.*

import _root_.sup.*

import scala.concurrent.duration.*

object sup:

  def cached[F[_]: Monad, K, H[_]](key: K, optionTimeout: Option[FiniteDuration] = None)(
      cache: MemoryCache[F, K, HealthResult[H]]
  ): HealthCheckEndoMod[F, H] =
    _.transform { healthCheck =>
      cache.lookup(key).flatMap {
        case Some(health) =>
          health.pure[F]
        case None         =>
          val timeout = optionTimeout.flatMap(TimeSpec.fromDuration)
          healthCheck.flatMap(health => cache.insertWithTimeout(timeout)(key, health).as(health))
      }
    }

end sup
