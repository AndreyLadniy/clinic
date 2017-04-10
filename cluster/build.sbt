import Deps._
import sbt.Keys._

lazy val cluster = (project in file("."))
  .settings(
    dockerExposedPorts := Seq(9001),
    mainClass in Compile:= Some("ecommerce.cluster.ClusterStartupApp"),
    libraryDependencies ++= Seq(
      AkkaDDD.core,
      "com.typesafe.akka" %% "akka-cluster" % Version.akka,
      "com.typesafe.akka" %% "akka-cluster-tools" % Version.akka,
      "com.typesafe.akka" %% "akka-cluster-sharding" % Version.akka,
      "com.typesafe.akka" %% "akka-slf4j" % Version.akka
    )
  )
  .dependsOn("scheduling-contracts", "commons")
  .enablePlugins(ApplicationPlugin)