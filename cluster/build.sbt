import Deps._
import sbt.Keys._

lazy val cluster = (project in file("."))
  .settings(
    mainClass in Compile:= Some("ecommerce.cluster.ClusterStartupApp"),

    libraryDependencies ++= Seq(
      AkkaDDD.core
    )
  )
  .dependsOn("scheduling-contracts", "commons")
  .enablePlugins(ApplicationPlugin)