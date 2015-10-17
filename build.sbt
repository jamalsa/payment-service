import ReleaseTransformations._

lazy val http4sVersion = "0.10.0"
lazy val doobieVersion = "0.2.2"

lazy val root = (project in file(".")).
  enablePlugins(JavaServerAppPackaging).
  settings(
    name := "binangkit-payment",
    organization := "net.binangkit",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-argonaut" % http4sVersion,
      "io.argonaut" %% "argonaut" % "6.1",
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-contrib-hikari" % doobieVersion,
      "mysql" % "mysql-connector-java" % "5.1.36"
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