package liwec

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import liwec.htmlDslTypes._
import liwec.macroUtils._

package object htmlMacros {
    def scoped(vn: VNode): VNode = macro scopedImpl
    def scopedImpl(c: Context)(vn: c.Expr[VNode]) = {
        import c.universe._
        val className =
            enclosingClassSym(c).fullName.replaceAll("[^a-zA-Z0-9]", "-")
        q"liwec.htmlDslTypes.scope($className, $vn)"
    }
}
