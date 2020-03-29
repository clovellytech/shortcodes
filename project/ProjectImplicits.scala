import dependencies._
import sbt._
import sbt.Keys._
import sbt.librarymanagement.DependencyBuilders

object ProjectImplicits {
  implicit class CommonSettings(val p: Project) extends AnyVal {
    def commonSettings(): Project = {
      p.settings(
        Seq(
          crossScalaVersions  := Seq(scala212, scala213),
          organization := "com.clovellytech",
          resolvers ++= addResolvers,
          // Make sure every subproject is using a logging configuration.
          scalacOptions ++= options.scalacExtraOptionsForVersion(scalaVersion.value),
          libraryDependencies ++= compilerPluginsForVersion(scalaVersion.value),
        )
      )
    }
  }
}
