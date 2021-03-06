name := "akka-persistence-sql"

version := "1.0"

scalaVersion := "2.12.0"

val akkaVersion = "2.4.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka"   %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"   %% "akka-persistence"     % akkaVersion,
  "com.typesafe.akka"   %% "akka-stream"          % akkaVersion,
  "com.typesafe.akka"   %% "akka-persistence-query-experimental" % akkaVersion,
  "com.typesafe.akka"   %% "akka-persistence-tck" % akkaVersion  % "test",
  "com.typesafe.akka"   %% "akka-slf4j"           % akkaVersion  % "test",
  "com.typesafe.akka"   %% "akka-testkit"         % akkaVersion  % "test",
  "com.typesafe.akka"   %% "akka-stream-testkit"  % akkaVersion  % "test",
  "com.typesafe.slick"  %% "slick"                % "3.2.0-M2",
  "org.slf4j"            % "slf4j-nop"            % "1.6.4",
  "com.h2database"       % "h2"                   % "1.4.187",
  "mysql"                % "mysql-connector-java" % "6.0.5",
  "com.typesafe.slick"  %% "slick-hikaricp"       % "3.2.0-M2",
  "com.zaxxer"           % "HikariCP"             % "2.5.1",
  "com.googlecode.concurrentlinkedhashmap"  % "concurrentlinkedhashmap-lru" % "1.4.2"
)
    