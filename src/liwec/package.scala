package liwec

import scalajs.js
import scalajs.js.annotation._
import scalajs.js.|
import liwec.htmlDslTypes._
import liwec.domvm._

abstract class VNode extends js.Object with VNodeApplicable[ElementVNode] {
    def applyTo(vn: ElementVNode) = arrayApplicableHelper(this, vn)
}

abstract class Component extends js.Object with VNodeApplicable[ElementVNode] {
    def render(): VNode

    def applyTo(vn: ElementVNode) = {
        val self = this
        arrayApplicableHelper(
            new ViewVNode {
                @JSName("type")
                val nodeType: Int = 4
                val view = vm => () => self.render()
            },
            vn)
    }
}
