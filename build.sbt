name := "play_basics"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.mongodb" % "mongo-java-driver" % "2.10.1",
  "org.mongojack" %% "play-mongojack" % "2.0.0-RC2", 
  cache
) 

play.Project.playJavaSettings
