package liwec

import scalajs.js
import scalajs.js.annotation._
import scalajs.js.|
import org.scalajs.dom
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
                val view = vm => {
                    val component = Component.createSetProxy(self, vm)
                    () => component.render()
                }
            },
            vn)
    }
}

object Component {
    /** Creates a JS proxy which will schedule a ViewModel reload on each
     *  intercepted change to the object.
     *  Proxies are not created recursively to prevent a variety of problems
     *  (what if the object is mutated elsewhere, what if it is shared by two
     *  components...).
     */
    def createSetProxy[T <: js.Object](obj: T, vm: ViewModel): T =
        js.Dynamic.newInstance(js.Dynamic.global.Proxy)(obj, new js.Object {
            def set(target: js.Dictionary[js.Any],
                    key: String,
                    value: js.Any) = {
                target(key) = value
                queueRedraw(vm)
                true
            }
        }).asInstanceOf[T]

    def queueRedraw(vm: ViewModel) = {
        (vm.redrawQueued: Any) match {
            case true => ()
            case _ => {
                vm.redrawQueued = true
                dom.window.requestAnimationFrame(time => {
                    vm.redraw()
                    vm.redrawQueued = false
                })
            }
        }
    }
}
