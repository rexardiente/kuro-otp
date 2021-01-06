name := """kuro-otp"""

organization := "com.ejisan"

version := "0.0.1-SNAPSHOTS"

scalaVersion := "2.13.1"

crossScalaVersions := Seq("2.11.11", scalaVersion.value, "2.12.2")

scalacOptions in Compile in console := Nil

scalacOptions in Compile in doc ++= Seq(
  "-sourcepath", (baseDirectory in LocalProject("kuro-otp")).value.getAbsolutePath,
  "-doc-title", "Kuro OTP (HOTP, TOTP)",
  "-doc-footer", "Copyright (c) 2017 Ryo Ejima (ejisan), Apache License v2.0.",
  "-doc-source-url", "https://github.com/ejisan/kuro-otp€{FILE_PATH}.scala")

javacOptions ++= Seq("-source", "1.8")

testOptions in Test ++= Seq(
  Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
  Tests.Argument(TestFrameworks.JUnit, "-q", "-v"))

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",
  "commons-codec" % "commons-codec" % "1.10",
  "junit" % "junit" % "4.12" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test)
