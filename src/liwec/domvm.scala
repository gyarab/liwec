package liwec.domvm

import scalajs.js
import scalajs.js.annotation._
import scalajs.js.|
import liwec._

// These are *NOT* general bindings for domvm. They describe only a fraction
// of its functionality and often cover internal functions. The types are also
// not as general as they should be.

trait ElementVNode extends VNode {
    @JSName("type")
    val nodeType: Int
    val flags: Int // Bitfield
    val attrs: js.Dictionary[Any]
    val tag: String | js.Object
    var body: js.UndefOr[String | js.Array[js.Any]]
}

trait ViewVNode extends VNode {
    @JSName("type")
    val nodeType: Int
    val view: js.Function1[ViewModel, js.Function0[VNode]]
}

@js.native
@JSGlobal("domvm.ViewModel")
class ViewModel(view: js.Function1[ViewModel, js.Function0[VNode]],
                data: js.Any = js.undefined,
                key: js.Any = js.undefined,
                opts: js.Any = js.undefined) extends js.Object {
    var redrawQueued: js.UndefOr[Boolean] = js.native // Added by liwec
    def redraw(): Unit = js.native
}
