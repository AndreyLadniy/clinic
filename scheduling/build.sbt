import Deps._

lazy val scheduling = (project in file(".")).aggregate(`scheduling-contracts`, `scheduling-write-back`, `scheduling-write-front`, `scheduling-read-back`, `scheduling-read-front`)

lazy val `scheduling-contracts` = (project in file("contracts"))
  .settings(
    libraryDependencies += AkkaDDD.messaging
  )

lazy val `scheduling-write-back` = (project in file("write-back"))
  .settings(
//    dockerExposedPorts := Seq(9101),
    mainClass in Compile:= Some("ecommerce.scheduling.app.SchedulingBackendApp"),
//    multiNodeTestingSettings,
    libraryDependencies ++=
      Seq(AkkaDDD.core, AkkaDDD.test, AkkaDDD.eventStore, AkkaDDD.monitoring, AkkaDDD.scheduling)
  )
  .dependsOn(`scheduling-contracts`, "commons")
//  .configs(MultiJvm)
  .enablePlugins(ApplicationPlugin)


lazy val `scheduling-write-front` = (project in file("write-front"))
  .settings(
//    dockerExposedPorts := Seq(9100),
//    javaOptions in Universal ++= Seq("-DmainClass=ecommerce.sales.app.SalesFrontApp"),
    mainClass in Compile:= Some("ecommerce.scheduling.app.SchedulingFrontApp"),
    libraryDependencies += AkkaDDD.writeFront
  )
  .dependsOn(`scheduling-contracts`, "commons")
  .enablePlugins(HttpServerPlugin)

lazy val `scheduling-read-back` = (project in file("read-back"))
  .settings(
//    javaOptions in Universal ++= Seq("-DmainClass=ecommerce.sales.app.SalesViewUpdateApp"),
    libraryDependencies ++= AkkaDDD.viewUpdateSql ++ Seq(AkkaDDD.eventStore)
  )
  .dependsOn(`scheduling-contracts`, "commons")
  .enablePlugins(ApplicationPlugin)


lazy val `scheduling-read-front` = (project in file("read-front"))
  .settings(
//    javaOptions in Universal ++= Seq("-DmainClass=ecommerce.sales.app.SalesReadFrontApp"),
//    dockerExposedPorts := Seq(9110)
  )
  .dependsOn(`scheduling-read-back` % "test->test;compile->compile", "commons")
  .enablePlugins(HttpServerPlugin)

