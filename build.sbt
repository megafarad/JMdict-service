ThisBuild / version := "0.1.2"

ThisBuild / scalaVersion := "2.13.10"

libraryDependencies ++= Dependencies.depends

lazy val root = (project in file("."))
  .settings(
    name := "JMdict-service",
    Docker / packageName := "jmdict-service",
    dockerBaseImage := "amazoncorretto:19-alpine",
    dockerUpdateLatest := true,
    dockerExposedPorts := Seq(9000)
  )
  .enablePlugins(AshScriptPlugin, DockerPlugin)
