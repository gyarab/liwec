// This file is part of liwec and is licenced under the MPL v2.0
// (c) 2019 David Koňařík

package liwec

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context
import liwec.htmlDsl._
import liwec.htmlDslHelpers._
import liwec.macroUtils._

package object htmlMacros {
    def scoped(vn: VNode): VNode = macro scopedImpl
    def scopedImpl(c: Context)(vn: c.Expr[VNode]) = {
        import c.universe._
        val className =
            enclosingClassSym(c).fullName.replaceAll("[^a-zA-Z0-9]", "-")
        q"liwec.htmlDslHelpers.scopeVNode($className, $vn)"
    }
}
