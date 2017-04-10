import Deps._

lazy val scheduling = (project in file(".")).aggregate(`scheduling-contracts`, `scheduling-write-back`, `scheduling-write-front`, `scheduling-read-back`, `scheduling-read-front`)

lazy val `scheduling-contracts` = (project in file("contracts"))
  .settings(
    libraryDependencies ++= Seq(
      AkkaDDD.messaging,
      "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % "2.8.5",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.8.5",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.8.5"
    )
  )

lazy val `scheduling-write-back` = (project in file("write-back"))
  .settings(
    dockerExposedPorts := Seq(9101),
    mainClass in Compile:= Some("ecommerce.scheduling.app.SchedulingBackendApp"),
//    multiNodeTestingSettings,
    libraryDependencies ++=
      Seq(AkkaDDD.core, AkkaDDD.test, AkkaDDD.eventStore, AkkaDDD.monitoring, AkkaDDD.scheduling)
  )
  .dependsOn(`scheduling-contracts`, "commons")
  .configs(MultiJvm)
  .enablePlugins(ApplicationPlugin)


lazy val `scheduling-write-front` = (project in file("write-front"))
  .settings(
    dockerExposedPorts := Seq(9100),
//    javaOptions in Universal ++= Seq("-DmainClass=ecommerce.sales.app.SalesFrontApp"),
    mainClass in Compile:= Some("ecommerce.scheduling.app.SchedulingFrontApp"),
    libraryDependencies ++=
      Seq(
        AkkaDDD.core,
        AkkaDDD.writeFront,
        "ch.megard" %% "akka-http-cors" % "0.1.11"
      )
  )
  .dependsOn(`scheduling-contracts`, "commons")
  .enablePlugins(HttpServerPlugin)

lazy val `scheduling-read-back` = (project in file("read-back"))
  .settings(
//    javaOptions in Universal ++= Seq("-DmainClass=ecommerce.sales.app.SalesViewUpdateApp"),
    mainClass in Compile:= Some("ecommerce.scheduling.app.SchedulingViewUpdateApp"),
    libraryDependencies ++= AkkaDDD.viewUpdateSql ++ Seq(AkkaDDD.eventStore)
  )
  .dependsOn(`scheduling-contracts`, "commons")
  .enablePlugins(ApplicationPlugin)


lazy val `scheduling-read-front` = (project in file("read-front"))
  .settings(
//    javaOptions in Universal ++= Seq("-DmainClass=ecommerce.sales.app.SchedulingReadFrontApp"),
    dockerExposedPorts := Seq(9110),
    mainClass in Compile:= Some("ecommerce.scheduling.app.SchedulingReadFrontApp"),
    libraryDependencies ++=
      Seq(
        "com.github.mauricio"  %% "postgresql-async" % "0.2.21",
        "ch.megard" %% "akka-http-cors" % "0.1.11"
      )

  )
  .dependsOn(`scheduling-read-back` % "test->test;compile->compile", "commons")
  .enablePlugins(HttpServerPlugin)

