inThisBuild(
  Seq(
    scalaVersion := "2.12.3",
    sonatypeGithub := ("fommil", "stalactite"),
    licenses := Seq(LGPL3)
  )
)

libraryDependencies ++= Seq(
  "org.scala-lang"       % "scala-compiler" % scalaVersion.value % "provided",
  "org.scala-lang"       % "scala-reflect"  % scalaVersion.value % "provided",
  "com.github.mpilquist" %% "simulacrum"    % "0.11.0"           % "test",
  "com.chuusai"          %% "shapeless"     % "2.3.2"            % "test",
  "org.typelevel"        %% "export-hook"   % "1.2.0"            % "test",
  "com.typesafe.play"    %% "play-json"     % "2.6.3"            % "test"
)

scalacOptions in Test += {
  val custom = Map(
    "stalactite.examples.Cobar" -> "stalactite.examples.CustomCobar.go"
  ).map { case (from, to) => s"$from=$to" }.mkString("|", "|", "|")
  s"-Xmacro-settings:stalactite=$custom"
}

scalacOptions ++= Seq(
  "-language:_",
  "-unchecked",
  "-explaintypes",
  "-Ywarn-value-discard",
  "-Ywarn-numeric-widen",
  "-Ypartial-unification",
  "-Xlog-free-terms",
  "-Xlog-free-types",
  "-Xlog-reflective-calls",
  "-Yrangepos",
  "-Yno-imports",
  "-Yno-predef"
)

scalacOptions := scalacOptions.value.filterNot(_.startsWith("-Ywarn-unused"))
scalacOptions += "-Ywarn-unused:-implicits,imports,locals,-params,patvars,privates"

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)

wartremoverWarnings in (Compile, compile) := Seq(
  Wart.FinalCaseClass,
  Wart.ExplicitImplicitTypes
)
wartremoverWarnings in (Test, compile) := (wartremoverWarnings in (Compile, compile)).value

scalafmtOnCompile in ThisBuild := true
scalafmtConfig in ThisBuild := file("project/scalafmt.conf")
scalafmtVersion in ThisBuild := "1.2.0"

scalacOptions in (Compile, console) -= "-Xfatal-warnings"
initialCommands in (Compile, console) := Seq(
  "java.lang.String",
  "scala.{Any,AnyRef,AnyVal,Boolean,Byte,Double,Float,Short,Int,Long,Char,Symbol,Unit,Null,Nothing,Option,Some,None,Either,Left,Right,StringContext}",
  "scala.annotation.tailrec",
  "scala.collection.immutable.{Map,Seq,List,::,Nil,Set,Vector}",
  "scala.util.{Try,Success,Failure}",
  "scala.Predef.{???,ArrowAssoc,identity,implicitly,<:<,=:=}",
  "shapeless.{ :: => :*:, _ }",
  "_root_.io.circe",
  "scalaz._",
  "Scalaz._"
).mkString("import ", ",", "")

addCommandAlias("fmt", ";sbt:scalafmt ;scalafmt ;test:scalafmt")
