scalaVersion := "2.12.8"

scalacOptions += "-deprecation"
scalacOptions += "-feature"

val defaults = Seq(
    scalaSource in Compile := baseDirectory.value / "src",
    organization := "mocasys.liwec",
    version := "1.0.0",
)

lazy val root = (project in file("."))
    .settings(
        defaults,
        name := "root",
        libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
        //scalacOptions += "-P:scalajs:sjsDefinedByDefault",
        //scalaJsUseMainModuleInitializer := true,
    )
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(macros, liwec)
    .aggregate(macros, liwec)

lazy val sample = project
    .settings(
        defaults,
        skip in publish := true,
        name := "sample",
    )
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(root)
    .aggregate(root)

lazy val liwec = project
    .settings(
        defaults,
        name := "liwec",
        libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6",
    )
    .enablePlugins(ScalaJSPlugin)

lazy val macros = project
    .settings(
        defaults,
        name := "macros",
        libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.12.6",
    )
    .aggregate(liwec)
    .dependsOn(liwec)

lazy val htmlCodegen = project
    .settings(
        defaults,
        name := "htmlCodegen",
        skip in publish := true,
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
        defaults,
        name := "cssCodegen",
        skip in publish := true,
        libraryDependencies ++= Seq(
            "com.softwaremill.sttp" %% "core" % "1.5.11",
            "com.lihaoyi" %% "upickle" % "0.7.1",
        ),
    )
