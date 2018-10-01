scalaVersion := "2.12.6" //TODO: Scala.js is not yet updated
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

lazy val codegen = (project in file("codegen"))
    .settings(
        name := "codegen",
        scalaSource in Compile := baseDirectory.value / "src",
        resolvers +=
            "Millhouse Bintray"
                at "http://dl.bintray.com/themillhousegroup/maven",
        libraryDependencies ++= Seq(
            "org.jsoup" % "jsoup" % "1.11.3",
            "com.themillhousegroup" %% "scoup" % "0.4.6",
        ),
        scalacOptions += "-deprecation",
        scalacOptions += "-feature",
    )