val scalazVersion     = "7.2.19"
val shapelessVersion  = "2.3.3"
val simulacrumVersion = "0.12.0"

addCommandAlias("cpl", "all compile test:compile")
addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias(
  "check",
  "all headerCheck test:headerCheck scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"
)
addCommandAlias("lint", "all compile:scalafixCli test:scalafixCli")

val deriving = (project in file("deriving-macro")).settings(
  name := "deriving-macro",
  MacroParadise,
  libraryDependencies ++= Seq(
    "org.scala-lang"       % "scala-compiler" % scalaVersion.value % "provided",
    "org.scala-lang"       % "scala-reflect"  % scalaVersion.value % "provided",
    "org.scalaz"           %% "scalaz-core"   % scalazVersion      % "test",
    "com.chuusai"          %% "shapeless"     % shapelessVersion   % "test",
    "org.ensime"           %% "pcplod"        % "1.2.1"            % "test",
    "com.github.mpilquist" %% "simulacrum"    % simulacrumVersion  % "test",
    "com.typesafe.play"    %% "play-json"     % "2.6.8"            % "test"
  )
)

// extensions to scalaz7.2
val scalaz = (project in file("scalaz-deriving-base")).settings(
  KindProjector,
  name := "scalaz-deriving-base",
  licenses := Seq(
    ("BSD-3" -> url("https://opensource.org/licenses/BSD-3-Clause"))
  ),
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless"   % shapelessVersion,
    "org.scalaz"  %% "scalaz-core" % scalazVersion
  )
)

val derivez = (project in file("scalaz-deriving"))
  .enablePlugins(NeoJmhPlugin)
  .dependsOn(
    scalaz,
    deriving % "test"
  )
  .settings(
    KindProjector,
    MacroParadise,
    name := "scalaz-deriving",
    envVars in Jmh += ("RANDOM_DATA_GENERATOR_SEED" -> "0"),
    // WORKAROUND https://gitlab.com/fommil/sbt-sensible/issues/18
    dependencyClasspathAsJars in NeoJmhPlugin.JmhInternal ++= (fullClasspathAsJars in Jmh).value,
    libraryDependencies ++= Seq(
      "org.scala-lang"      % "scala-compiler"         % scalaVersion.value % "provided",
      "io.frees"            %% "iotaz-core"            % "0.3.6",
      "com.danielasfregola" %% "random-data-generator" % "2.4" % "test,jmh"
    )
  )

val xmlformat = (project in file("examples/xmlformat"))
  .dependsOn(deriving)
  .settings(
    MacroParadise,
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Yno-predef",
    libraryDependencies ++= Seq(
      "org.scalaz"             %% "scalaz-core" % scalazVersion,
      "com.chuusai"            %% "shapeless"   % shapelessVersion,
      "com.github.mpilquist"   %% "simulacrum"  % simulacrumVersion,
      "org.scala-lang.modules" %% "scala-xml"   % "1.1.0"
    )
  )

// root project
publishLocal := {}
publish := {}
