package liwec

import liwec.htmlDslTypes._

package object htmlDsl {
    // TODO: Generate on compile time from HTML spec
    val cls = Attr[Seq[String]]("class")
    val id = Attr[String]("id")

    trait VNodeTagDiv extends VNode
    trait VNodeTagSpan extends VNode
    val div = Tag[VNodeTagDiv]("div")
    val span = Tag[VNodeTagSpan]("span")
}
