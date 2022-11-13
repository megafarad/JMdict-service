import sbt._

object Version {
  val akka = "2.7.0"
  val slick = "3.4.1"
  val postgres = "42.5.0"
  val akkaHttp = "10.4.0"
  val quartz = "2.3.2"
  val json4s = "4.0.6"
  val akkaHttpJson4s = "1.39.2"
  val logbackClassic = "1.4.4"
  val moji4j = "1.2.0"
  val orgJson = "20220924"
  val jwksRsa = "0.21.2"
  val jwtCore = "9.1.1"
  val jwtJson4s = "9.1.2"
  val scalaTest = "3.2.14"

}

object Library {
  val akkaActor = "com.typesafe.akka"      %% "akka-actor-typed"      % Version.akka
  val akkaStream = "com.typesafe.akka"     %% "akka-stream"           % Version.akka
  val slick = "com.typesafe.slick"         %% "slick"                 % Version.slick
  val slickHikariCP = "com.typesafe.slick" %% "slick-hikaricp"        % Version.slick
  val postgresql = "org.postgresql"         % "postgresql"            % Version.postgres
  val akkaHttp = "com.typesafe.akka"       %% "akka-http"             % Version.akkaHttp
  val quartz = "org.quartz-scheduler"       % "quartz"                % Version.quartz
  val orgJson =  "org.json"                 % "json"                  % Version.orgJson
  val json4sNative = "org.json4s"          %% "json4s-native"         % Version.json4s
  val json4sJackson = "org.json4s"         %% "json4s-jackson"        % Version.json4s
  val akkaHttpJson4s = "de.heikoseeberger" %% "akka-http-json4s"      % Version.akkaHttpJson4s
  val logbackClassic = "ch.qos.logback"     % "logback-classic"       % Version.logbackClassic
  val moji4j = "com.andree-surya"           % "moji4j"                % Version.moji4j
  val jwksRsa = "com.auth0"                 % "jwks-rsa"              % Version.jwksRsa
  val jwtCore = "com.github.jwt-scala"     %% "jwt-core"              % Version.jwtCore
  val jwtJson4s = "com.github.jwt-scala"   %% "jwt-json4s-native"     % Version.jwtJson4s
  val scalaTest = "org.scalatest"          %% "scalatest"             % Version.scalaTest % Test
}

object Dependencies {

  import Library._

  val depends: Seq[ModuleID] = Seq(
    akkaActor,
    akkaStream,
    slick,
    slickHikariCP,
    postgresql,
    akkaHttp,
    quartz,
    orgJson,
    json4sNative,
    json4sJackson,
    akkaHttpJson4s,
    logbackClassic,
    moji4j,
    jwksRsa,
    jwtCore,
    jwtJson4s,
    scalaTest
  )

}
