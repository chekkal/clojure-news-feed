organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "redis.clients"	      %	  "jedis"		    %	"2.7.2", 
    "org.scala-lang.modules"  %%  "scala-parser-combinators"  % "1.0.3",
    "mysql"               %   "mysql-connector-java"  % "5.0.8",
    "postgresql"          %   "postgresql"    % "9.1-901.jdbc4",
    "com.datastax.cassandra"  % "cassandra-driver-core" % "2.1.4",
    "org.apache.kafka"    %   "kafka-clients"    % "0.8.2.0",
    "com.mchange"         %   "c3p0"          % "0.9.5",
    "com.dynamicalsoftware"   %   "feed.support.services"   %   "0.0.1-SNAPSHOT", 
    "org.apache.solr"	      %	  "solr-core"		    %	"5.3.1", 
    "org.apache.solr"	      %	  "solr-solrj"		    %	"5.3.1"
  )
}

Revolver.settings
