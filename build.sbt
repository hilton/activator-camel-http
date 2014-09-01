name := "camel-http"

version := "1.0"

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.apache.camel" % "camel-core" % "2.13.2",
  "org.apache.camel" % "camel-scala" % "2.13.2",
  "org.apache.camel" % "camel-http4" % "2.13.2",
  "org.apache.httpcomponents" % "httpmime" % "4.3.5",
  "org.slf4j" % "slf4j-api" % "1.7.7"
)
