import sbt._

object Version {
  val pekko = "1.0.3"
  val slick = "3.5.0"
  val postgres = "42.7.3"
  val pekkoHttp = "1.0.1"
  val quartz = "2.3.2"
  val json4s = "4.0.7"
  val pekkoHttpJson4s = "2.6.0"
  val logbackClassic = "1.5.6"
  val moji4j = "1.2.0"
  val orgJson = "20220924"
  val jwksRsa = "0.22.1"
  val jwtCore = "10.0.1"
  val jwtJson4s = "10.0.0"
  val scalaTest = "3.2.19"

}

object Library {
  val pekkoActor = "org.apache.pekko"      %% "pekko-actor-typed"     % Version.pekko
  val pekkoStream = "org.apache.pekko"     %% "pekko-stream"          % Version.pekko
  val slick = "com.typesafe.slick"         %% "slick"                 % Version.slick
  val slickHikariCP = "com.typesafe.slick" %% "slick-hikaricp"        % Version.slick
  val postgresql = "org.postgresql"         % "postgresql"            % Version.postgres
  val pekkoHttp = "org.apache.pekko"       %% "pekko-http"            % Version.pekkoHttp
  val quartz = "org.quartz-scheduler"       % "quartz"                % Version.quartz
  val orgJson =  "org.json"                 % "json"                  % Version.orgJson
  val json4sNative = "org.json4s"          %% "json4s-native"         % Version.json4s
  val json4sJackson = "org.json4s"         %% "json4s-jackson"        % Version.json4s
  val pekkoHttpJson4s = "com.github.pjfanning" %% "pekko-http-json4s" % Version.pekkoHttpJson4s
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
    pekkoActor,
    pekkoStream,
    slick,
    slickHikariCP,
    postgresql,
    pekkoHttp,
    quartz,
    orgJson,
    json4sNative,
    json4sJackson,
    pekkoHttpJson4s,
    logbackClassic,
    moji4j,
    jwksRsa,
    jwtCore,
    jwtJson4s,
    scalaTest
  )

}
