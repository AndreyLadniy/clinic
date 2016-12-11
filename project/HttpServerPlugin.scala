import Deps._
import sbt.Keys._
import sbt._

object HttpServerPlugin extends AutoPlugin {

  override def requires = ApplicationPlugin

  override lazy val projectSettings = Seq(
    libraryDependencies ++= AkkaDDD.httpSupport,
    parallelExecution in Test := false
  )
}