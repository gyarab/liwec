package liwec

import scala.language.implicitConversions
import scalajs.js
import scalajs.js.annotation._
import scalajs.js.JSConverters._
import liwec.domvm._

package object htmlDslTypes {
    trait VNodeApplicable[-T <: ElementVNode] extends js.Object {
        def applyTo(vn: T): Unit
    }

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

    object Implicits {
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

    def toJsType(x: Any) = x match {
        case x: Seq[_] => x.toJSArray
        case x => x
    }
    case class Attr[-N <: ElementVNode, T](name: String) {
        class AttrWithValue(value: T) extends VNodeApplicable[N] {
            def applyTo(vn: N) = { vn.attrs(name) = toJsType(value) }
        }
        def := (value: T) =
            new AttrWithValue(value)
    }
}
