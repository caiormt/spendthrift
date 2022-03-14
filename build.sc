import mill._
import mill.scalalib._

import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`

import $ivy.`com.lihaoyi::mill-contrib-flyway:$MILL_VERSION`
import mill.contrib.flyway.FlywayModule

import $ivy.`com.goyeau::mill-scalafix_mill0.10:0.2.8`
import com.goyeau.mill.scalafix.StyleModule

import $ivy.`io.github.davidgregory084::mill-tpolecat_mill0.10:0.3.0`
import io.github.davidgregory084.TpolecatModule

private object Versions {
  val scalaVersion = "3.1.1"

  // Core
  val cats       = "2.7.0"
  val catsEffect = "3.3.6"

  val catsTime = "0.4.0"
  val kittens  = "3.0.0-M3"

  // Data
  val squants = "1.8.3"

  // Serializing
  val circe = "0.15.0-M1"

  // Http
  val http4s = "0.23.10"

  // Database
  val skunk      = "0.3.1"
  val postgresql = "42.3.3"

  // Testing
  val munit           = "1.0.0-M2"
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

object `spendthrift-application` extends CommonModule {

  object test extends CommonTestModule

  override def artifactName = "spendthrift-application"

  override def moduleDeps =
    Seq(`spendthrift-adapters`, `spendthrift-web`)
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
      ivy"org.typelevel::cats-effect::${Versions.catsEffect}"
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
      ivy"io.circe::circe-parser::${Versions.circe}"
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
      ivy"org.http4s::http4s-circe::${Versions.http4s}"
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
