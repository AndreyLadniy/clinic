import Deps._
import sbt.Keys._

lazy val `api-gateway` = (project in file(".")).aggregate(`web-api-gateway`)

lazy val `web-api-gateway` = (project in file("web"))
  .settings(
    dockerExposedPorts := Seq(9002),
    mainClass in Compile:= Some("ecommerce.gateway.api.web.WebApiGatewayApp"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.0.5",
      "ch.megard" %% "akka-http-cors" % "0.1.11",
      "com.typesafe.akka" %% "akka-slf4j" % Version.akka
    )
  )
  .dependsOn("commons")
  .enablePlugins(ApplicationPlugin)
