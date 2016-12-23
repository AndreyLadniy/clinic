import Deps._

lazy val core = (project in file("."))
  .settings(
    //    multiNodeTestingSettings,
    libraryDependencies ++=
      Seq(AkkaDDD.core)
  )