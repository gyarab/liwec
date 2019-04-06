// This file is part of liwec and is licenced under the MPL v2.0
// (c) 2019 David Koňařík

package liwec.test

import scalajs.js
import scalajs.js.JSConverters._
import scalajs.js.annotation._
import liwec.htmlDsl._
import liwec.cssMacros._
import liwec.htmlMacros._
import liwec._

case class Task(val text: String, val completed: Boolean = false)

class TaskComponent(var task: Task, val onDelete: () => Unit)
        extends Component {
    cssScoped { import liwec.cssDsl._
        c.btn -> (
            color := "turquoise",
        )
    }
    def render() = scoped(
        li(onclick:={_ => task = task.copy(completed = !task.completed)},
            span(task.text),
            span(if(task.completed) img(alt:="C") else Seq()),
            span("X", cls:="btn", onclick:={_ => onDelete()})
        )
    )
}

class TodoDemo(var tasks: js.Array[Task]) extends Component {
    css { import liwec.cssDsl._
        e.h1 -> (
            fontSize := "20pt",
        )
    }

    def render() = {
        div(
            h1("TODO list"),
            ul(tasks.syncMapWithIndex[TaskComponent](
                _.task,
                { case (t, i) =>
                    new TaskComponent(t, () => tasks.remove(i)) })),
        )
    }
}

object Test {
    @JSExportTopLevel("scalaJsTest")
    def test() = {
        div(new TodoDemo(Seq(
            Task("Learn Scala"),
            Task("Use liwec in a project"),
        ).toJSArray))
    }
}
