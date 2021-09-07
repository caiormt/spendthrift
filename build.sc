import mill._
import mill.scalalib._

import $ivy.`com.goyeau::mill-scalafix:0.2.5`
import com.goyeau.mill.scalafix.StyleModule

import $ivy.`io.github.davidgregory084::mill-tpolecat:0.2.0`
import io.github.davidgregory084.TpolecatModule

private object Versions {
  val scalaVersion = "3.0.2"

  // Core
  val cats       = "2.6.1"
  val catsEffect = "3.2.7"

  // Testing
  val munit           = "0.7.29"
  val munitCatsEffect = "1.0.5"

  // Scalafix
  val organizeImports = "0.5.0"
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
}

object `spendthrift-core` extends CommonModule {

  object test extends CommonTestModule

  override def artifactName = "spendthrift-core"

  override def ivyDeps =
    Agg(
      ivy"org.typelevel::cats-core::${Versions.cats}",
      ivy"org.typelevel::cats-effect::${Versions.catsEffect}"
    )
}

trait CommonModule extends SbtModule with StyleModule with TpolecatModule {

  trait CommonTestModule extends Tests with TestModule.Munit {
    override def ivyDeps =
      Agg(
        ivy"org.scalameta::munit::${Versions.munit}",
        ivy"org.scalameta::munit-scalacheck::${Versions.munit}",
        ivy"org.typelevel::munit-cats-effect-3::${Versions.munitCatsEffect}"
      )
  }

  override def scalaVersion = Versions.scalaVersion

  override def scalacOptions =
    super.scalacOptions() diff Seq("-Xfatal-warnings")

  override def scalafixIvyDeps =
    Agg(
      ivy"com.github.liancheng::organize-imports:${Versions.organizeImports}"
    )
}
