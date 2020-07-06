import sbt._, Keys._
import sbt.librarymanagement.DependencyBuilders

object dependencies {
  val scala212 = "2.12.11"
  val scala213 = "2.13.1"

  val addResolvers = Seq(
    Resolver.sonatypeRepo("public")
  )

  object versions {
    val betterMonadicFor = "0.3.1"
    val cats = "2.1.1"
    val catsEffect = "2.1.2"
    val fs2 = "2.3.0"
    val kindProjector212 = "0.10.3"
    val kindProjector213 = "0.11.0"
    val macroParadise = "2.1.1"
    val scalaCheck = "1.15.0-e5dc7d1-SNAPSHOT"
    val scalaTest = "3.2.0"
    val scalaTestPlusScalacheck = "3.1.0.0-RC2"
    val simulacrum = "1.0.0"
  }

  def compilerPlugins = Seq(
    compilerPlugin("com.olegpy" %% "better-monadic-for" % versions.betterMonadicFor)
  )

  def compilerPluginsForVersion(version: String) =
    CrossVersion.partialVersion(version) match {
      case Some((2, major)) if major < 13 =>
        compilerPlugins ++ Seq(
          compilerPlugin("org.scalamacros" % "paradise" % versions.macroParadise cross CrossVersion.full),
          compilerPlugin("org.typelevel" %% "kind-projector" % versions.kindProjector212)
        )
      case Some((2, major)) if major == 13 => compilerPlugins ++ Seq(
        compilerPlugin("org.typelevel" % s"kind-projector_$version" % versions.kindProjector213)
      )
      case _ => compilerPlugins
    }

  val testDeps = Seq(
    "org.scalatest" %% "scalatest" % versions.scalaTest,
    "org.scalatestplus" %% "scalatestplus-scalacheck" % versions.scalaTestPlusScalacheck,
    "org.scalacheck" %% "scalacheck" % versions.scalaCheck,
  )

  val testDepsInTestOnly = testDeps.map(_ % "test")

  val commonDeps = Seq(
    "org.typelevel" %% "cats-core" % versions.cats,
    "org.typelevel" %% "cats-effect" % versions.catsEffect,
    "org.typelevel" %% "simulacrum" % versions.simulacrum,
    "co.fs2" %% "fs2-core" % versions.fs2,
    "co.fs2" %% "fs2-io" % versions.fs2,
  ) ++ testDeps
}
