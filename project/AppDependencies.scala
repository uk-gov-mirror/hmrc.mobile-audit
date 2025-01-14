import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val play26Bootstrap    = "1.3.0"
  private val playHmrcApiVersion = "4.1.0-play-26"
  private val catsCore           = "1.6.0"

  private val pegdownVersion                 = "1.6.0"
  private val wireMockVersion                = "2.21.0"
  private val scalaMockVersion               = "4.1.0"
  private val scalaTestVersion               = "3.0.5"
  private val scalaTestPlusVersion           = "3.1.2"
  private val playJsonJodaVersion            = "2.6.13"
  private val playJsonSchemaValidatorVersion = "0.9.5"
  private val scalaCheckVersion              = "1.14.0"
  private val integrationTestVersion         = "0.9.0-play-26"
  private val refinedVersion                 = "0.9.4"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"   %% "bootstrap-play-26" % play26Bootstrap,
    "uk.gov.hmrc"   %% "play-hmrc-api"     % playHmrcApiVersion,
    "org.typelevel" %% "cats-core"         % catsCore,
    "eu.timepit"    %% "refined"           % refinedVersion
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalamock"     %% "scalamock"                  % scalaMockVersion               % "test",
    "org.scalatest"     %% "scalatest"                  % scalaTestVersion               % "test",
    "com.typesafe.play" %% "play-test"                  % current                        % "test",
    "org.pegdown"       % "pegdown"                     % pegdownVersion                 % "test, it",
    "org.scalacheck"    %% "scalacheck"                 % scalaCheckVersion              % "test, it",
    "com.typesafe.play" %% "play-json-joda"             % playJsonJodaVersion            % "test, it",
    "com.eclipsesource" %% "play-json-schema-validator" % playJsonSchemaValidatorVersion % "test"
  )

  val it: Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"                % scalaTestVersion       % "it",
    "com.typesafe.play"      %% "play-test"                % current                % "it",
    "org.pegdown"            % "pegdown"                   % pegdownVersion         % "it",
    "uk.gov.hmrc"            %% "service-integration-test" % integrationTestVersion % "it",
    "com.github.tomakehurst" % "wiremock"                  % wireMockVersion        % "it",
    "org.scalatestplus.play" %% "scalatestplus-play"       % scalaTestPlusVersion   % "it"
  )
}
