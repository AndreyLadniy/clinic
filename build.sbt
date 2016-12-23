name := "clinic"

organization in ThisBuild := "com.rudux"

version in ThisBuild := "0.1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

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

addCommandAlias("redeploy", ";clean;docker:stage;restart")
addCommandAlias("redeployQuick", ";docker:stage;restart")

addCommandAlias("rswb", ";project scheduling-write-back;docker:stage;restart")
addCommandAlias("rswf", ";project scheduling-write-front;docker:stage;restart")