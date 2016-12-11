name := "clinic"

organization in ThisBuild := "com.rudux"

version in ThisBuild := "0.1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

sourcesInBase in ThisBuild := false

lazy val root = project.settings(
  aggregate in update := false
)
  .aggregate(commons, scheduling)

lazy val commons = project

lazy val scheduling = project.dependsOn(commons)

lazy val headquarters = project.dependsOn(commons)
