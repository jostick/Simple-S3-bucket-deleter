import sbt._


lazy val commonSettings = Seq(
  organization := "com.autoscout24",
  version := Option(System.getenv("GO_PIPELINE_LABEL")) getOrElse "1.0-SNAPSHOT",
  scalaVersion in ThisBuild := "2.11.8",

  scalacOptions in ThisBuild ++= Seq("-unchecked",
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-Xmax-classfile-name", "130"),
  // skip generation of api scaladocs
  doc in Compile <<= target.map(_ / "none"),
  // skip generation of api javadocs
  publishArtifact in (Compile, packageDoc) := false,
  // skip packaging of sources
  publishArtifact in (Compile, packageSrc) := false,
  // AWS credentials are needed for local tests on the dynamoDB
  javaOptions in ThisBuild ++= Seq("-Daws.accessKeyId=USERID", "-Daws.secretKey=PASSWORD")
)

name := "delete-s3-bucket"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-s3" % "1.10.64",
  "com.typesafe" % "config" % "1.3.0"
)

javaOptions in Runtime ++= Seq(
  "-Dconfig.resource=as24dev.conf"
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    cleanFiles <+= baseDirectory { _ / "logs" }
  )


