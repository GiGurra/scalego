lazy val commonSettings = Seq(
  organization := "se.gigurra",
  version := "SNAPSHOT",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),
  libraryDependencies ++= Seq(
    "org.scala-lang"       %    "scala-reflect"         %   scalaVersion.value,
    "org.scalatest"        %%   "scalatest"             %   "2.2.4"     %   "test",
    "org.mockito"           %   "mockito-core"          %   "1.10.19"   %   "test"
  )
)

lazy val scalego_core = Project(
  id = "scalego-core",
  base = file("scalego-core"),
  settings = commonSettings
)

lazy val scalego_serialization = Project(
  id = "scalego-serialization",
  base = file("scalego-serialization"),
  settings = commonSettings,
  dependencies = Seq(scalego_core)
)

lazy val scalego_serialization_json = Project(
  id = "scalego-serialization-json",
  base = file("scalego-serialization-json"),
  settings = commonSettings,
  dependencies = Seq(scalego_core, scalego_serialization)
).settings(
  libraryDependencies ++= Seq(
    "org.json4s"  %%  "json4s-core"     %   "3.4.0",
    "org.json4s"  %%  "json4s-jackson"  %   "3.4.0"
  )
)

lazy val scalego = Project(id = "scalego", base = file(".")).aggregate(
  scalego_core,
  scalego_serialization
)
