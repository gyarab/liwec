package liwec.test

import scalajs.js.annotation._
import liwec.htmlDslTypes.Implicits._
import liwec.htmlDsl._

object Test {
    @JSExportTopLevel("scalaJsTest")
    def test() = {
        val el =
            div(id := "x", cls := Seq("something"),
                span("Hello, world!")
            )
        el
    }
}
