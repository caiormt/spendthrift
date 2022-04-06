package spendthrift.application.config

import cats.*
import cats.derived.*
import cats.implicits.*

import ciris.*

import com.comcast.ip4s.*

object data:

  // format: off
  final case class DatabaseConfig(
      host: String,
      port: Int,
      username: String,
      database: String,
      password: Option[Secret[String]],
      max: Int
  ) derives Eq, Show
  // format: on

  // format: off
  final case class HttpConfig(
      host: Host,
      port: Port
  ) derives Eq, Show
  // format: on

  // format: off
  final case class AuthenticationConfig(
      jwtSecret: Secret[String]
  ) derives Eq, Show
  // format: on

  // format: off
  final case class AppConfig(
      database: DatabaseConfig,
      http: HttpConfig,
      auth: AuthenticationConfig
  ) derives Eq, Show
  // format: on

  val databaseConfig: ConfigValue[Effect, DatabaseConfig] =
    (
      env("DATABASE_HOST").as[String].default("127.0.0.1"),
      env("DATABASE_PORT").as[Int].default(5432),
      env("DATABASE_USERNAME").as[String].default("spendthrift"),
      env("DATABASE_NAME").as[String].default("spendthrift"),
      env("DATABASE_PASSWORD").as[String].secret.option.default(Secret("spendthrift@dev").some),
      env("DATABASE_MAX_CONNECTIONS").as[Int].default(Math.max(2, Runtime.getRuntime.availableProcessors()))
    ).parMapN(DatabaseConfig.apply)

  def host(envName: String): ConfigValue[Effect, Host] =
    env(envName).as[String].flatMap { host =>
      Host.fromString(host) match {
        case Some(host) => ConfigValue.default(host)
        case None       => ConfigValue.failed(ConfigError("Wrong Host string format"))
      }
    }

  def port(envName: String): ConfigValue[Effect, Port] =
    env(envName).as[Int].flatMap { port =>
      Port.fromInt(port) match {
        case Some(port) => ConfigValue.default(port)
        case None       => ConfigValue.failed(ConfigError("Wrong Host string format"))
      }
    }

  val httpConfig: ConfigValue[Effect, HttpConfig] =
    (
      host("HTTP_SERVER_HOST").default(host"0.0.0.0"),
      port("HTTP_SERVER_PORT").default(port"8081")
    ).parMapN(HttpConfig.apply)

  val authConfig: ConfigValue[Effect, AuthenticationConfig] = {
    val secret = env("AUTH_JWT_SECRET").as[String].secret.default(Secret("spendthrift"))

    secret.map(AuthenticationConfig.apply)
  }

  val appConfig: ConfigValue[Effect, AppConfig] =
    (databaseConfig, httpConfig, authConfig).parMapN(AppConfig.apply)

end data
