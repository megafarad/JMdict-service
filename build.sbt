ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

libraryDependencies ++= Dependencies.depends

lazy val root = (project in file("."))
  .settings(
    name := "JMdict-service"
  )
