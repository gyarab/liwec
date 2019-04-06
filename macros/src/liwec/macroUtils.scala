package liwec

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

package object macroUtils {
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

}
