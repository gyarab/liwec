package liwec.test

import scalajs.js
import scalajs.js.annotation._
import liwec.htmlDslTypes.Implicits._
import liwec.htmlDsl._
import liwec._

class Counter(var n: Int) extends Component {
    def render() =
        ul(for(i <- (1 to n)) yield
            li(s"Item no $i")
            //s"Item no $i"
        )
}

object Test {
    @JSExportTopLevel("scalaJsTest")
    def test() = {
        val el =
            div(id := "x", cls := "something",
                onclick := {e => js.Dynamic.global.alert(e) },
                span("Hello, world!"),
                img(src := "funny.gif"),
                new Counter(8),
            )
        el
    }
}
