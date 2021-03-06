import SbtProjectInfo._

ThisBuild / organization := "io.kevinlee"
ThisBuild / scalaVersion := props.ProjectScalaVersion
ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}")
  )
)
ThisBuild / homepage := url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}").some
ThisBuild / scmInfo :=
  ScmInfo(
    url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}"),
    s"https://github.com/${props.GitHubUsername}/${props.RepoName}.git"
  ).some
ThisBuild / licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))

lazy val maven2sbt = (project in file("."))
  .enablePlugins(DevOopsGitHubReleasePlugin, DocusaurPlugin)
  .settings(
    name := props.RepoName,
    libraryDependencies := libraryDependenciesPostProcess(scalaVersion.value, libraryDependencies.value),
    /* GitHub Release { */
    devOopsPackagedArtifacts := List(
      s"cli/target/universal/${name.value}*.zip",
      "cli/target/native-image/maven2sbt-cli-*",
    ),
    /* } GitHub Release */
    /* Website { */
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
    gitHubPagesOrgName := props.GitHubUsername,
    gitHubPagesRepoName := props.RepoName
    /* } Website */
  )
  .settings(noPublish)
  .aggregate(core, cli)

lazy val core = subProject("core", file("core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    crossScalaVersions := props.CrossScalaVersions,
    libraryDependencies ++= {
      val scalaV = scalaVersion.value
      if (scalaV.startsWith("3.0"))
        List(libs.catsLib, libs.scalaXmlLatest)
      else if (scalaV.startsWith("2.11"))
        List(libs.newTypeLib, libs.cats_2_0_0, libs.scalaXml)
      else
        List(libs.newTypeLib, libs.catsLib, libs.scalaXmlLatest)
    },
    libraryDependencies ++= Seq(
      libs.effectieCatsEffect,
    ),
    libraryDependencies := libraryDependenciesPostProcess(scalaVersion.value, libraryDependencies.value),
    wartremoverExcluded ++= (if (scalaVersion.value.startsWith("3.")) List.empty else List(sourceManaged.value)),
    /* Build Info { */
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoObject := "Maven2SbtBuildInfo",
    buildInfoPackage := s"${props.RepoName}.info",
    buildInfoOptions += BuildInfoOption.ToJson,
    /* } Build Info */
  )

lazy val pirate = ProjectRef(props.pirateUri, "pirate")

lazy val cli = subProject("cli", file("cli"))
  .enablePlugins(JavaAppPackaging, NativeImagePlugin)
  .settings(
    libraryDependencies := libraryDependenciesPostProcess(scalaVersion.value, libraryDependencies.value),
    scalaVersion := (ThisBuild / scalaVersion).value,
    maintainer := "Kevin Lee <kevin.code@kevinlee.io>",
    packageSummary := "Maven2Sbt",
    packageDescription := "A tool to convert Maven pom.xml into sbt build.sbt",
    executableScriptName := props.ExecutableScriptName,
    //    nativeImageVersion := "21.1.0",
    //    nativeImageJvm := "graalvm-java11",
    nativeImageOptions ++= Seq(
      "-H:+ReportExceptionStackTraces",
      "--initialize-at-build-time",
      "--verbose",
      "--no-fallback",
      //      "--report-unsupported-elements-at-runtime",
      //      "--allow-incomplete-classpath",
      //      "--initialize-at-build-time=scala.runtime.Statics",
      //      "--initialize-at-build-time=scala.Enumeration.populateNameMap",
      //      "--initialize-at-build-time=scala.Enumeration.getFields$1",
    ),
  )
  .settings(noPublish)
  .dependsOn(core, pirate)


// format: off
def prefixedProjectName(name: String) =
  s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// format: on

val removeDottyIncompatible: ModuleID => Boolean =
  m =>
    m.name == "wartremover" ||
      m.name == "ammonite" ||
      m.name == "kind-projector" ||
      m.name == "mdoc" ||
      m.name == "better-monadic-for"

lazy val props =
  new {

    final val GitHubUsername       = "Kevin-Lee"
    final val RepoName             = "maven2sbt"
    final val ExecutableScriptName = RepoName

    final val DottyVersion        = "3.0.0"
    //final val ProjectScalaVersion = "2.13.5"
    final val ProjectScalaVersion = DottyVersion
    final val CrossScalaVersions  = List("2.12.14", "2.13.6", ProjectScalaVersion, DottyVersion).distinct

    final val hedgehogVersion = "0.7.0"

    final val canEqualVersion = "0.1.0"

    final val EffectieVersion = "1.11.0"

    final val pirateVersion = "main"
    final val pirateUri     = uri(s"https://github.com/$GitHubUsername/pirate.git#$pirateVersion")

    final val scalaXml1 = "1.3.0"
    final val scalaXml2 = "2.0.0"

  }

lazy val libs =
  new {
    lazy val hedgehogLibs: List[ModuleID] = List(
      "qa.hedgehog" %% "hedgehog-core"   % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.hedgehogVersion % Test,
    )

    lazy val canEqual = "io.kevinlee" %% "can-equal" % props.canEqualVersion

    lazy val scalaXmlLatest = "org.scala-lang.modules" %% "scala-xml" % props.scalaXml2
    lazy val scalaXml       = "org.scala-lang.modules" %% "scala-xml" % props.scalaXml1

    lazy val catsLib   = "org.typelevel" %% "cats-core" % "2.6.1"
    lazy val cats_2_0_0 = "org.typelevel" %% "cats-core" % "2.0.0"

    lazy val catsEffectLib   = "org.typelevel" %% "cats-effect" % "2.5.1"
    lazy val catsEffect_2_0_0 = "org.typelevel" %% "cats-effect" % "2.0.0"

    lazy val effectieCatsEffect   = "io.kevinlee" %% "effectie-cats-effect"   % props.EffectieVersion
    lazy val effectieScalazEffect = "io.kevinlee" %% "effectie-scalaz-effect" % props.EffectieVersion

    lazy val newTypeLib = "io.estatico" %% "newtype" % "0.4.4"
  }

def libraryDependenciesPostProcess(
  scalaVersion: String,
  libraries: Seq[ModuleID]
): Seq[ModuleID] =
  if (scalaVersion.startsWith("3.0")) {
    libraries
      .filterNot(removeDottyIncompatible)
  } else {
    libraries
  }

lazy val scala3cLanguageOptions =
  "-language:" + List(
    "dynamics",
    "existentials",
    "higherKinds",
    "reflectiveCalls",
    "experimental.macros",
    "implicitConversions",
  ).mkString(",")

def scalacOptionsPostProcess(scalaVersion: String, options: Seq[String]): Seq[String] =
  if (scalaVersion.startsWith("3.0")) {
    Seq(
//      "-source:3.0-migration",
      scala3cLanguageOptions,
      "-Ykind-projector",
      "-siteroot",
      "./dotty-docs",
    )
  } else {
    options
  }

def subProject(projectName: String, path: File): Project =
  Project(projectName, path)
    .settings(
      name := prefixedProjectName(projectName),
      testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework")),
      libraryDependencies ++= libs.hedgehogLibs,
      scalacOptions := scalacOptionsPostProcess(scalaVersion.value, scalacOptions.value).distinct,
      Compile / unmanagedSourceDirectories ++= {
        val sharedSourceDir = baseDirectory.value / "src/main"
        if (scalaVersion.value.startsWith("2."))
          Seq(
            sharedSourceDir / "scala-2.12_2.13",
          )
        else
          Seq.empty
      },
      /* WartRemover and scalacOptions { */
//      , Compile / compile / wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value)
//      , Test / compile / wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value)
      wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value),
//            , wartremoverErrors ++= Warts.all
      Compile / console / wartremoverErrors := List.empty,
      Compile / console / wartremoverWarnings := List.empty,
      Compile / console / scalacOptions :=
        (console / scalacOptions)
          .value
          .distinct
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      Test / console / wartremoverErrors := List.empty,
      Test / console / wartremoverWarnings := List.empty,
      Test / console / scalacOptions :=
        (console / scalacOptions)
          .value
          .distinct
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
//      , Compile / compile / wartremoverExcluded += sourceManaged.value
//      , Test / compile / wartremoverExcluded += sourceManaged.value
      /* } WartRemover and scalacOptions */
      licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))
    )
