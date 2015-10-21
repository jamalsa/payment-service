import ReleaseTransformations._

lazy val http4sVersion = "0.10.1"
lazy val doobieVersion = "0.2.2"

lazy val root = (project in file(".")).
  enablePlugins(JavaServerAppPackaging).
  settings(
    name := "binangkit-payment",
    organization := "net.binangkit",
    version := "0.3.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-argonaut" % http4sVersion,
      "io.argonaut" %% "argonaut" % "6.1",
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-contrib-hikari" % doobieVersion,
      "mysql" % "mysql-connector-java" % "5.1.36",
      "com.typesafe" % "config" % "1.3.0",
      "ch.qos.logback" % "logback-classic" % "1.0.13",
      "org.log4s" %% "log4s" % "1.1.5",
      "org.scalaj" %% "scalaj-http" % "1.1.5"
    ),
    seq(Revolver.settings: _*),
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,              // : ReleaseStep
      inquireVersions,                        // : ReleaseStep
      runTest,                                // : ReleaseStep
      setReleaseVersion,                      // : ReleaseStep
      commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
      tagRelease,                             // : ReleaseStep
      setNextVersion,                         // : ReleaseStep
      commitNextVersion                      // : ReleaseStep
    )
  )