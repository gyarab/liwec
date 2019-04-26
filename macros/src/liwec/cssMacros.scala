// This file is part of liwec and is licenced under the MPL v2.0
// (c) 2019 David Koňařík

package liwec

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import scala.language.dynamics
import liwec.macroUtils._

package object cssMacros {
    import scala.collection.mutable
    import java.nio.file.{Paths, Files, Path}
    import liwec.cssDslTypes._

    // This code hopes that PWD is the SBT project dir
    def getCssOutDir() = {
        def getOutDir(startPath: Path): Path = {
            val path = startPath.resolve("target")
            if(Files.isDirectory(path)) {
                return path
            } else {
                startPath.getParent() match {
                    case null => {
                        throw new Exception(
                            "Failed to find \"target\" directory")
                    }
                    case p => return getOutDir(p)
                }
            }
        }
        val dirTarget = getOutDir(Paths.get("."))
        val dirCss = dirTarget.resolve("css")
        if(!Files.exists(dirCss)) {
            Files.createDirectories(dirCss)
        }
        dirCss
    }

    def getCssOutPath(className: String) = {
        getCssOutDir().resolve(className + ".css")
    }

    def cssBlockToRules(c: Context)(body: c.Tree) = {
        import c.universe._
        val Block((ruleTreesBody, ruleTreeRes)) = body
        val ruleTrees = ruleTreesBody :+ ruleTreeRes
        for {
            ruleTree <- ruleTrees
            rule <- ruleTree match {
                    // Skip imports and evaluate everything else into
                    // a CSS Rule
                    case i: Import => List()
                    case _ => {
                        val urt = c.untypecheck(ruleTree)
                        List(c.eval(c.Expr[Rule](urt)))
                    }
                }
            cssRule <- rule.toCssRules()
        } yield cssRule
    }

    // Taken mostly from
    // https://github.com/cb372/scalacache/blob/309f5ef8a16b0dae81ecf50643b8bdf7ad8f4eb1/modules/core/shared/src/main/scala/scalacache/memoization/Macros.scala
    def enclosingClassSym(c: Context): c.Symbol = {
        def enclosingClassSymInner(sym: c.Symbol): c.Symbol = {
          if (sym == null)
            c.abort(
                c.enclosingPosition,
                "Encountered a null symbol while searching for enclosing class")
          else if (sym.isClass || sym.isModule)
            sym
          else
            enclosingClassSymInner(sym.owner)
        }

        enclosingClassSymInner(c.internal.enclosingOwner)
    }

    def processCss(c: Context)(body: c.Tree, scoped: Boolean) = {
        import c.universe._
        val className =
            enclosingClassSym(c).fullName.replaceAll("[^a-zA-Z0-9]", "-")

        val path = getCssOutPath(className)
        if(Files.exists(path)) {
            val firstLine = Files.lines(path).findFirst().get()
            if(!firstLine.startsWith("/* ")) {
                throw new Exception("Corrupted CSS file")
            }
            // TODO: Caching
        }
        //else {
        val rules =
            if(scoped) {
                val scopeSel = RawSelector(s"[scope=$className]")
                for(CssRule(sel, props) <- cssBlockToRules(c)(body))
                    yield CssRule(scopeCss(scopeSel, sel), props)
            } else {
                cssBlockToRules(c)(body)
            }
        val cssStr = rules.map(_.toString).mkString("\n")
        Files.write(path, ("/* TODO */\n" + cssStr).getBytes())
        //}

        q""
    }

    def css(body: Rule): Unit = macro cssImpl
    def cssImpl(c: Context)(body: c.Tree): c.Tree = processCss(c)(body, false)

    def cssScoped(body: Rule): Unit = macro cssScopedImpl
    def cssScopedImpl(c: Context)(body: c.Tree): c.Tree =
        processCss(c)(body, true)
}
