val http4sVersion = "0.10.0"

lazy val root = (project in file(".")).
  settings(
    name := "binangkit-payment",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion
    ),
    seq(Revolver.settings: _*)
  )