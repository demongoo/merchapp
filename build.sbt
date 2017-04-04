name := "Merchant App"

version := "0.1"

scalaVersion := "2.11.8"

val h2 = "com.h2database" % "h2" % "1.2.127"

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "8.0.102-R11",
  "org.squeryl" %% "squeryl" % "0.9.5-7",
  h2
  //"com.typesafe.slick" %% "slick" % "3.0.0",
  //"org.slf4j" % "slf4j-nop" % "1.6.4"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature", "-language:_")

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true
