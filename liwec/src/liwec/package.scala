// This file is part of liwec and is licenced under the MPL v2.0
// (c) 2019 David Koňařík

package liwec

import scalajs.js
import scalajs.js.annotation._
import scalajs.js.|
import org.scalajs.dom
import liwec.htmlDsl._
import liwec.htmlDslHelpers._
import liwec.domvm._

abstract class VNode extends js.Object with VNodeApplicable[ElementVNode] {
    def applyTo(vn: ElementVNode) = arrayApplicableHelper(this, vn)
}

abstract class Watched extends js.Object {
    var changeCallbacks = js.Array[this.type => Unit]()
    /** Sets a callback for every time this object is mutated */
    def onChange(callback: this.type => Unit): this.type = {
        this.changeCallbacks.append(callback)
        this
    }

    var fieldChangeCallbacks = js.Dictionary[js.Array[this.type => Unit]]()
    /** Sets a callback for every time this object's field is mutated */
    def onFieldChange(fieldName: String, callback: this.type => Unit): this.type = {
        this.fieldChangeCallbacks(fieldName).append(callback)
        this
    }

    /** Creates a JS proxy which will fire event handlers on each intercepted
     *  change to the object.
     *  Proxies are not created recursively (except for arrays)
     *  to prevent a variety of problems (what if the object is mutated
     *  elsewhere, what if it is shared by two components...).
     */
    def createSetProxy(): this.type = {
        // The ugliness is here to work around some weird Scala semantics
        val self = this
        var p: self.type = null

        def shouldProxy(obj: Any) =
            obj match {
                case _: js.Array[_] => true
                case _ => false
            }

        def proxied[T <: js.Any](obj: T): T =
            js.Dynamic.newInstance(js.Dynamic.global.Proxy)(obj,
                new js.Object {
                    def set(target: js.Dictionary[js.Any],
                            key: String,
                            value: js.Any): Boolean = {
                        // Stop unnecessary events
                        if(key == "changeCallbacks"
                           || key == "fieldChangeCallbacks") return true
                        fieldChangeCallbacks.get(key) match {
                            case None => fieldChangeCallbacks(key) = js.Array()
                            // TODO: Is the cast here the right call? And why
                            // is it not necessary for the other callbacks?
                            case Some(cs) => for(c <- cs)
                                c(p.asInstanceOf[Watched.this.type])
                        }
                        target(key) =
                            if(shouldProxy(value)) proxied(value) else value
                        for(c <- self.changeCallbacks) c(p)
                        true
                    }
            }).asInstanceOf[T]

        p = proxied(self)
        // Create proxies for already present collections
        val pDict = p.asInstanceOf[js.Dictionary[js.Any]]
        for((key, value) <- pDict) {
            if(shouldProxy(value)) pDict(key) = proxied(value)
        }
        p.asInstanceOf[this.type]
    }
}

abstract class Component extends Watched with VNodeApplicable[ElementVNode] {
    // vm will be set when the component is added into the VDom
    var vm: Option[ViewModel] = None

    def render(): VNode
    def onMount() = ()

    def toVNode() = {
        val self = this
        new ViewVNode {
            @JSName("type")
            val nodeType: Int = 4
            val view = vm => {
                self.vm = Some(vm)
                val component = self.createSetProxy(vm)
                component.onMount()
                () => component.render()
            }
        },
    }

    def applyTo(vn: ElementVNode) = {
        arrayApplicableHelper(this.toVNode(), vn)
    }

    /** Creates a JS proxy which will schedule a ViewModel reload on each
     *  intercepted change to the object. */
    def createSetProxy(vm: ViewModel): this.type = {
        this.createSetProxy()
        .onChange { self =>
            Component.queueRedraw(vm)
        }
    }
}

object Component {
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
