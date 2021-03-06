organization := "info.glennengstrand"
name := "scalatra-news-feed"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.12.4"
scalacOptions += "-Ypartial-unification"

mainClass in assembly := Some("JettyMain")

val ScalatraVersion = "2.6.2"

libraryDependencies ++= Seq(
  "org.scalatra"      %% "scalatra"             % ScalatraVersion,
  "org.scalatra"      %% "scalatra-swagger"     % ScalatraVersion,
  "org.scalatra"      %% "scalatra-scalatest"   % ScalatraVersion % Test,
  "org.json4s"        %% "json4s-jackson"       % "3.5.0",
  "org.eclipse.jetty" %  "jetty-server"         % "9.4.8.v20171121",
  "org.eclipse.jetty" %  "jetty-webapp"         % "9.4.8.v20171121",
  "javax.servlet"     %  "javax.servlet-api"    % "3.1.0",
  "org.typelevel" %% "cats-core" % "1.0.1", 
  "org.tpolecat" %% "doobie-core"      % "0.5.0",
  "org.tpolecat" %% "doobie-hikari"    % "0.5.0",  
  "mysql" % "mysql-connector-java" % "5.1.34",
  "redis.clients"	      %	  "jedis"		    %	"2.9.0", 
  "ch.qos.logback"    %  "logback-classic"      % "1.2.3" % Provided
)

enablePlugins(JettyPlugin)