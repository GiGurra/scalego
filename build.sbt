val scalego = Project(id = "scalego", base = file("."))
  .settings(
    organization := "se.gigurra",
    version := "SNAPSHOT",

    scalaVersion := "2.11.8",

    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),

    libraryDependencies ++= Seq(
      "org.scalatest"        %%   "scalatest"             %   "2.2.4"     %   "test",
      "org.mockito"           %   "mockito-core"          %   "1.10.19"   %   "test"
    )
    
  )
