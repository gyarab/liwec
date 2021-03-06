// This file is part of liwec and is licenced under the MPL v2.0
// (c) 2019 David Koňařík

package liwec

import scala.language.implicitConversions
import scalajs.js
import scalajs.js.annotation._
import scalajs.js.JSConverters._
import liwec.domvm._

package htmlDsl {
    import htmlDslHelpers._

    trait VNodeApplicable[-T <: ElementVNode] extends js.Object {
        def applyTo(vn: T): Unit
    }

    trait Implicits {
        implicit class VNodeApplicableString(str: String)
                extends VNodeApplicable[ElementVNode] {
            def applyTo(vn: ElementVNode) = (vn.body: Any) match {
                    case body: js.Array[_] => {
                        body.asInstanceOf[js.Array[js.Any]].push(str)
                    }
                    case null | () => { vn.body = str }
                }
        }
        implicit class VNodeApplicableTraversableOnce[-T <: ElementVNode]
                (travOnce: TraversableOnce[VNodeApplicable[T]])
                extends VNodeApplicable[T] {
            def applyTo(vn: T) = for(appl <- travOnce) appl.applyTo(vn)
        }
        implicit class VNodeApplicableJsIterable[-T <: ElementVNode]
                (iterable: js.Iterable[VNodeApplicable[T]])
                extends VNodeApplicable[T] {
            def applyTo(vn: T) = for(appl <- iterable) appl.applyTo(vn)
        }
        implicit class VNodeApplicableOption[-T <: ElementVNode]
                (option: Option[T])
                extends VNodeApplicable[T] {
            def applyTo(vn: T) = for(appl <- option) appl.applyTo(vn)
        }
        implicit class JsArraySyncMap[T](arr: js.Array[T]) {
            def syncMapWithIndex[C <: Component]
                    (itemGetter: C => T, constructor: (T, Int) => C) =
                for((item, i) <- arr.zipWithIndex) yield
                    constructor(item, i) onChange { c =>
                        arr(i) = itemGetter(c)
                    }
            def syncMap[C <: Component]
                    (itemGetter: C => T, constructor: T => C) =
                syncMapWithIndex(
                    itemGetter, { case (item, _) => constructor(item) })
        }
    }

    case class Tag[T <: ElementVNode](name: String) {
        def apply(applicables: VNodeApplicable[T]*) = {
            var vnode = (new ElementVNode {
                @JSName("type")
                val nodeType = 1
                val flags = 0 // TODO
                val attrs = js.Dictionary.empty
                val tag = name
                var body = ()
            }).asInstanceOf[T]
            for(applicable <- applicables) {
                applicable.applyTo(vnode)
            }
            vnode
        }
    }

    case class Attr[-N <: ElementVNode, T](name: String) {
        class AttrWithValue(value: T) extends VNodeApplicable[N] {
            def applyTo(vn: N) = { vn.attrs(name) = toJsType(value) }
        }
        def := (value: T) =
            new AttrWithValue(value)
    }
}

package object htmlDslHelpers {
    def arrayApplicableHelper(addee: js.Any, vn: ElementVNode) =
        (vn.body: Any) match {
            case body: String => {
                vn.body = js.Array[js.Any](body, addee)
            }
            case body: js.Array[_] => {
                body.asInstanceOf[js.Array[js.Any]].push(addee)
            }
            case null | () => { vn.body = js.Array[js.Any](addee) }
        }

    def toJsType(x: Any) = x match {
        case x: Seq[_] => x.toJSArray
        case x => x
    }

    def scopeVNode(className: String, vn: VNode) = {
        def addScopeAttr(vn: ElementVNode): Unit = {
            if(vn.nodeType != 1) { // Element
                return
            }
            vn.attrs("scope") = className
            (vn.body: Any) match {
                case children: js.Array[_] =>
                    for(child <- children)
                        child match {
                            case _: js.Object => addScopeAttr(child.asInstanceOf[ElementVNode])
                            case _ => {}
                        }
                case _ => {}
            }
        }
        addScopeAttr(vn.asInstanceOf[ElementVNode])
        vn
    }
}
