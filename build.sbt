import Deps._

name := "clinic"

organization in ThisBuild := "com.rudux"

version in ThisBuild := "0.1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.1"

sourcesInBase in ThisBuild := false

lazy val root = project.settings(
  aggregate in update := false
)
  .aggregate(commons, scheduling)

lazy val core = project

lazy val commons = project

lazy val cluster = project.dependsOn(commons)

lazy val scheduling = project.dependsOn(commons)

lazy val headquarters = project.dependsOn(commons)

lazy val `view-update-mongodb` = project.settings(
  libraryDependencies ++=
    Seq(
      "org.reactivemongo" %% "reactivemongo" % "0.12.1",
      "pl.newicom.dddd" %% "view-update" % Version.akkaDDD
    )
)

lazy val `api-gateway` = project.dependsOn(commons)

addCommandAlias("redeploy", ";clean;docker:stage;restart")
addCommandAlias("redeployQuick", ";docker:stage;restart")

addCommandAlias("rswb", ";project scheduling-write-back;docker:stage;restart")
addCommandAlias("rswf", ";project scheduling-write-front;docker:stage;restart")