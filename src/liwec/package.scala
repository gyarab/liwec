package liwec

import scalajs.js
import scalajs.js.annotation._
import scalajs.js.|

trait VNode extends js.Object {
    @JSName("type")
    val nodeType: Int
    val flags: Int // Bitfield
    val attrs: js.Dictionary[Any]
    val tag: String | js.Object
    var body: js.UndefOr[String | js.Array[js.Any]]
}

trait Component extends js.Object {
    def render(): VNode
}
