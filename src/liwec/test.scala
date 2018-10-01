package liwec.test

import scalajs.js
import scalajs.js.annotation._
import liwec.htmlDslTypes.Implicits._
import liwec.htmlDsl._

object Test {
    @JSExportTopLevel("scalaJsTest")
    def test() = {
        val el =
            div(id := "x", cls := "something",
                onclick := {e => js.Dynamic.global.alert(e) },
                span("Hello, world!"),
                img(src := "funny.gif")
            )
        el
    }
}
