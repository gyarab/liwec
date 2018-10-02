package liwec.test

import scalajs.js
import scalajs.js.annotation._
import liwec.htmlDslTypes.Implicits._
import liwec.htmlDsl._
import liwec._

class Counter(var n: Int) extends Component {
    def render() = {
        var m = 0
        ul(for(i <- (1 to n)) yield
            // TODO: Deal with compiler bug (n += 1 doesn't work)
            li(s"Item no $i", onclick := {e =>
                this.n = this.n + 1
                js.Dynamic.global.console.log("D1", n)
            })
            //s"Item no $i"
        )
    }
}

object Test {
    @JSExportTopLevel("scalaJsTest")
    def test() = {
        val el =
            div(id := "x", cls := "something",
                //onclick := {e => js.Dynamic.global.alert(e) },
                span("Hello, world!"),
                img(src := "funny.gif"),
                new Counter(3),
            )
        el
    }
}
