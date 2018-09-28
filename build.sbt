scalaVersion := "2.12.6"
organization := "smcds"

lazy val root = (project in file("."))
    .settings(
        name := "liwec",
		scalaSource in Compile := baseDirectory.value / "src",
        libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
        //scalacOptions += "-P:scalajs:sjsDefinedByDefault",
        scalacOptions += "-deprecation",
        scalacOptions += "-feature",
		//scalaJsUseMainModuleInitializer := true,
    )
	.enablePlugins(ScalaJSPlugin)
	//.enablePlugins(ScalaJSBundlerPlugin)
