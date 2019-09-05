inThisBuild(
  List(
    organization := "com.kubukoz",
    homepage := Some(url("https://github.com/kubukoz/boilerstate")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "kubukoz",
        "Jakub Kozłowski",
        "kubukoz@gmail.com",
        url("https://kubukoz.com")
      )
    )
  ))

val commonSettings = Seq(
  scalaVersion := "2.12.8",
  scalacOptions ++= Options.all,
  fork in Test := true,
  name := "boilerstate",
  updateOptions := updateOptions.value.withGigahorse(false), //may fix publishing bug
  libraryDependencies ++= Seq(
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full),
    "org.typelevel"        %% "cats-tagless-macros" % "0.1.0",
    "co.fs2"               %% "fs2-core"            % "1.0.3",
    "com.github.mpilquist" %% "simulacrum"          % "0.14.0",
    "com.olegpy"           %% "meow-mtl"            % "0.2.0",
    "com.github.gvolpe"    %% "console4cats"        % "0.6.0",
    "org.scalatest"        %% "scalatest"           % "3.0.5" % Test
  )
)

val core = project.settings(commonSettings).settings(name += "-core")

val boilerstate =
  project.in(file(".")).settings(commonSettings).settings(skip in publish := true).dependsOn(core).aggregate(core)
