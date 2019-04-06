scalaVersion := "2.12.6" //TODO: Scala.js is not yet updated
organization := "smcds"

scalacOptions += "-deprecation"
scalacOptions += "-feature"

lazy val root = (project in file("."))
    .settings(
        name := "liwec",
        scalaSource in Compile := baseDirectory.value / "src",
        libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
        //scalacOptions += "-P:scalajs:sjsDefinedByDefault",
        //scalaJsUseMainModuleInitializer := true,
    )
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(macros, liwec)
    .aggregate(macros, liwec)

lazy val liwec = project
    .settings(
        name := "liwec",
        scalaSource in Compile := baseDirectory.value / "src",
        libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
    )
    .enablePlugins(ScalaJSPlugin)

lazy val macros = project
    .settings(
        name := "macros",
        scalaSource in Compile := baseDirectory.value / "src",
        libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.12.6",
    )
    .aggregate(liwec)
    .dependsOn(liwec)

lazy val htmlCodegen = project
    .settings(
        name := "htmlCodegen",
        scalaSource in Compile := baseDirectory.value / "src",
        resolvers +=
            "Millhouse Bintray"
                at "http://dl.bintray.com/themillhousegroup/maven",
        libraryDependencies ++= Seq(
            "org.jsoup" % "jsoup" % "1.11.3",
            "com.themillhousegroup" %% "scoup" % "0.4.6",
        ),
    )

lazy val cssCodegen = project
    .settings(
        name := "cssCodegen",
        scalaSource in Compile := baseDirectory.value / "src",
        libraryDependencies ++= Seq(
            "com.softwaremill.sttp" %% "core" % "1.5.11",
            "com.lihaoyi" %% "upickle" % "0.7.1",
        ),
    )
