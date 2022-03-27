import mill._
import mill.scalalib._

import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`

import $ivy.`com.lihaoyi::mill-contrib-flyway:$MILL_VERSION`
import mill.contrib.flyway.FlywayModule

import $ivy.`com.lihaoyi::mill-contrib-docker:$MILL_VERSION`
import mill.contrib.docker.DockerModule

import $ivy.`com.goyeau::mill-scalafix_mill0.10:0.2.8`
import com.goyeau.mill.scalafix.StyleModule

import $ivy.`io.github.davidgregory084::mill-tpolecat_mill0.10:0.3.0`
import io.github.davidgregory084.TpolecatModule

private object Versions {
  val scalaVersion = "3.1.1"

  // Core
  val cats       = "2.7.0"
  val catsEffect = "3.3.9"

  val catsTime = "0.4.0"
  val kittens  = "3.0.0-M3"

  // Data
  val squants = "1.8.3"

  // Serializing
  val circe = "0.15.0-M1"

  // Http
  val http4s = "0.23.11"

  // Database
  val skunk      = "0.3.1"
  val postgresql = "42.3.3"

  // Logging
  val log4cats = "2.2.0"
  val log4j    = "2.17.2"

  // Healthcheck
  val sup = "0.9.0-M6"

  // Caching
  val mules = "0.5.0"

  // Configuration
  val ciris = "2.3.2"

  // Metrics
  val epimetheus       = "0.5.0-M2"
  val epimetheusHttp4s = "0.6.0-M2"

  // Tracing
  val natchez       = "0.1.6"
  val natchezHttp4s = "0.3.2"

  // Testing
  val munit           = "1.0.0-M3"
  val munitCatsEffect = "1.0.7"

  // Scalafix
  val organizeImports = "0.6.0"
}

object project extends Module {

  /**
    * Update the millw script.
    */
  def millw() = T.command {
    val target = mill.modules.Util.download("https://raw.githubusercontent.com/lefou/millw/main/millw")
    val millw  = build.millSourcePath / "millw"
    os.copy.over(target.path, millw)
    os.perms.set(millw, os.perms(millw) + java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE)
    target
  }

  def millwbat() = T.command {
    val target = mill.modules.Util.download("https://raw.githubusercontent.com/lefou/millw/main/millw.bat")
    val millw  = build.millSourcePath / "millw.bat"
    os.copy.over(target.path, millw)
    os.perms.set(millw, os.perms(millw) + java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE)
    target
  }
}

object spendthrift extends FlywayModule {

  override def flywayUrl      = T.input(T.ctx.env.getOrElse[String]("FLYWAY_URL", "jdbc:postgresql://localhost/spendthrift"))
  override def flywayUser     = T.input(T.ctx.env.getOrElse[String]("FLYWAY_USER", "spendthrift"))
  override def flywayPassword = T.input(T.ctx.env.getOrElse[String]("FLYWAY_PASSWORD", "spendthrift@dev"))

  override def flywayDriverDeps =
    Agg(
      ivy"org.postgresql:postgresql:${Versions.postgresql}"
    )

  override def flywayFileLocations = T {
    `spendthrift-adapters`.resources().map(pr => PathRef(pr.path / "db" / "migration", pr.quick))
  }
}

object `spendthrift-adapters` extends CommonModule {

  object test extends CommonTestModule

  override def artifactName = "spendthrift-adapters"

  override def moduleDeps =
    Seq(`spendthrift-commands`, `spendthrift-queries`)

  override def ivyDeps =
    Agg(
      ivy"org.tpolecat::skunk-core::${Versions.skunk}",
      ivy"org.tpolecat::skunk-circe::${Versions.skunk}"
    )
}

object `spendthrift-application` extends CommonModule with DockerModule {

  object docker extends DockerConfig {

    override def tags = List("spendthrift")

    override def baseImage = "openjdk:17-jdk-alpine"

    override def exposedPorts = Seq(8081)
  }

  object test extends CommonTestModule

  override def artifactName = "spendthrift-application"

  override def moduleDeps =
    Seq(`spendthrift-adapters`, `spendthrift-web`)

  override def ivyDeps =
    Agg(
      ivy"io.chrisdavenport::mules-caffeine::${Versions.mules}",
      ivy"is.cir::ciris::${Versions.ciris}",
      ivy"org.tpolecat::natchez-jaeger::${Versions.natchez}"
    )

  override def runIvyDeps =
    Agg(
      ivy"org.apache.logging.log4j:log4j-api:${Versions.log4j}",
      ivy"org.apache.logging.log4j:log4j-core:${Versions.log4j}",
      ivy"org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4j}"
    )
}

object `spendthrift-commands` extends CommonModule {

  object test extends CommonTestModule

  override def artifactName = "spendthrift-commands"

  override def moduleDeps =
    Seq(`spendthrift-effects`, `spendthrift-domain`)
}

object `spendthrift-commons` extends CommonModule {

  object test extends CommonTestModule

  override def artifactName = "spendthrift-commons"

  override def ivyDeps =
    Agg(
      ivy"org.typelevel::cats-core::${Versions.cats}",
      ivy"io.chrisdavenport::cats-time::${Versions.catsTime}",
      ivy"org.typelevel::kittens::${Versions.kittens}",
      ivy"org.typelevel::squants::${Versions.squants}"
    )
}

object `spendthrift-domain` extends CommonModule {

  object test extends CommonTestModule

  override def artifactName = "spendthrift-domain"

  override def moduleDeps =
    Seq(`spendthrift-commons`)
}

object `spendthrift-effects` extends CommonModule {

  object test extends CommonTestModule

  override def artifactName = "spendthrift-effects"

  override def ivyDeps =
    Agg(
      ivy"org.typelevel::cats-effect::${Versions.catsEffect}",
      ivy"org.typelevel::log4cats-slf4j::${Versions.log4cats}",
      ivy"com.kubukoz::sup-core::${Versions.sup}",
      ivy"io.chrisdavenport::mules::${Versions.mules}",
      ivy"io.chrisdavenport::epimetheus::${Versions.epimetheus}",
      ivy"org.tpolecat::natchez-core::${Versions.natchez}"
    )
}

object `spendthrift-presentation` extends CommonModule {

  object test extends CommonTestModule

  override def artifactName = "spendthrift-presentation"

  override def moduleDeps =
    Seq(`spendthrift-commands`, `spendthrift-queries`)

  override def ivyDeps =
    Agg(
      ivy"io.circe::circe-core::${Versions.circe}",
      ivy"io.circe::circe-generic::${Versions.circe}",
      ivy"io.circe::circe-parser::${Versions.circe}",
      ivy"com.kubukoz::sup-circe::${Versions.sup}"
    )
}

object `spendthrift-queries` extends CommonModule {

  object test extends CommonTestModule

  override def artifactName = "spendthrift-queries"

  override def moduleDeps =
    Seq(`spendthrift-effects`, `spendthrift-domain`)
}

object `spendthrift-web` extends CommonModule {

  object test extends CommonTestModule

  override def artifactName = "spendthrift-web"

  override def moduleDeps =
    Seq(`spendthrift-presentation`)

  override def ivyDeps =
    Agg(
      ivy"org.http4s::http4s-dsl::${Versions.http4s}",
      ivy"org.http4s::http4s-ember-server::${Versions.http4s}",
      ivy"org.http4s::http4s-circe::${Versions.http4s}",
      ivy"io.chrisdavenport::epimetheus-http4s::${Versions.epimetheusHttp4s}",
      ivy"org.tpolecat::natchez-http4s::${Versions.natchezHttp4s}"
    )
}

trait BaseModule extends TpolecatModule with StyleModule {

  override def scalacOptions =
    T {
      super.scalacOptions().filterNot(Set("-Xsource:3", "-migration"))
    }

  override def scalafixIvyDeps =
    Agg(
      ivy"com.github.liancheng::organize-imports:${Versions.organizeImports}"
    )
}

trait CommonModule extends SbtModule with BaseModule {

  trait CommonTestModule extends Tests with TestModule.Munit with BaseModule {
    override def ivyDeps =
      Agg(
        ivy"org.scalameta::munit::${Versions.munit}",
        ivy"org.scalameta::munit-scalacheck::${Versions.munit}",
        ivy"org.typelevel::munit-cats-effect-3::${Versions.munitCatsEffect}"
      )
  }

  override def scalaVersion = Versions.scalaVersion
}
