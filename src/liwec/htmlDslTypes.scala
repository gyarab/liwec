package liwec

import scala.language.implicitConversions
import scalajs.js
import scalajs.js.annotation._
import scalajs.js.JSConverters._

package object htmlDslTypes {
    type VNodeApplicable = (VNode) => Unit

    object Implicits {
        implicit def vnode2applicable(vnode: VNode): VNodeApplicable =
            (vn: VNode) => (vn.body: Any) match {
                case body: String => {
                    vn.body = js.Array[js.Any](body, vnode)
                }
                case body: js.Array[_] => {
                    body.asInstanceOf[js.Array[js.Any]].push(vnode)
                }
                case null | () => { vn.body = js.Array[js.Any](vnode) }
            }
        implicit def string2applicable(str: String): VNodeApplicable =
            (vn: VNode) => {vn.body: Any} match {
                case body: js.Array[_] => {
                    body.asInstanceOf[js.Array[js.Any]].push(str)
                }
                case null | () => { vn.body = str }
            }
    }

    case class Tag[T <: VNode](name: String) {
        def apply(applicables: VNodeApplicable*) = {
            var vnode = new VNode {
                @JSName("type")
                val nodeType = 1
                val flags = 0 // TODO
                val attrs = js.Dictionary.empty
                val tag = name
                var body = ()
            }
            for(applicable <- applicables) {
                applicable(vnode)
            }
            vnode
        }
    }

    object AttrTypes {
    }

    def toJsType(x: Any) = x match {
        case x: Seq[_] => x.toJSArray
        case x => x
    }
    case class Attr[T](name: String) {
        def := (value: T) =
            (vnode: VNode) => { vnode.attrs(name) = toJsType(value) }
    }
}
