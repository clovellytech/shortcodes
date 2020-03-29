import dependencies._
import xerial.sbt.Sonatype._
import ProjectImplicits._


inThisBuild(
  Seq(
    organization := "com.clovellytech",
    homepage := Some(url("https://github.com/clovellytech/http4s-modules")),
    licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
    developers := List(
      Developer(
        "zakpatterson",
        "Zak Patterson",
        "pattersonzak@gmail.com",
        url("https://github.com/zakpatterson")
      )
    )
  )
)


lazy val shortcodes = project
  .in(file("./modules/shortcodes"))
  .settings(name := "shortcodes")
  .commonSettings()
  .settings(
    libraryDependencies ++= commonDeps,
    initialCommands in console := """
      |import cats._,cats.implicits._,cats.effect._
      |import com.clovellytech.shortcode._
      |import scala.concurrent.ExecutionContext.Implicits.{global => ec}
      |implicit val cs = IO.contextShift(ec)
      |val blocker = Blocker.liftExecutionContext(ec)
    """.stripMargin,
  )


lazy val root = project
  .in(file("."))
  .settings(name := "shortcodes")
  .commonSettings()
  .settings(
    skip in publish := true,
  )
  .dependsOn(shortcodes)
  .aggregate(shortcodes)