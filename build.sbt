name := """Wildvision"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, ElasticBeanstalkPlugin, BuildInfoPlugin)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.typesafe.play" %% "anorm" % "2.5.2",
  "mysql" % "mysql-connector-java" % "5.1.39",
  "com.github.nscala-time" %% "nscala-time" % "2.12.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

// Docker/Elastic Beanstalk
maintainer in Docker := "Wildvision.tv Webmaster <webmaster@wildvision.tv>"
dockerExposedPorts := Seq(9000)
dockerBaseImage := "java:latest"

// BuildInfoPlugin
buildInfoPackage := "build"