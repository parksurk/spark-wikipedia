//name := "spark-wikipedia"

//version := "0.1"

//scalaVersion := "2.12.8"


lazy val sparkVersion = "2.1.0"
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "wikipedia",
      scalaVersion := "2.11.8",
      version      := "1.0"
    )),
    name := "spark-wikipedia",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.scalatest" %% "scalatest" % "3.0.1"
    )
  )