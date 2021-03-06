import Deps._
import sbt.Keys._

lazy val headquarters = (project in file(".")).aggregate(`headquarters-write-back`)

lazy val `headquarters-write-back` = (project in file("write-back"))
  .settings(
    dockerExposedPorts := Seq(9401),
//    javaOptions in Universal += "-DmainClass=ecommerce.headquarters.app.HeadquartersApp",
    mainClass in Compile:= Some("ecommerce.headquarters.app.HeadquartersApp"),
    libraryDependencies ++=
      Seq(AkkaDDD.core, AkkaDDD.test, AkkaDDD.eventStore, AkkaDDD.scheduling)
  )
  .dependsOn("scheduling-contracts", "commons")
  .enablePlugins(ApplicationPlugin)