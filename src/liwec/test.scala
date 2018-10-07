package liwec.test

import scalajs.js
import scalajs.js.JSConverters._
import scalajs.js.annotation._
import liwec.htmlDslTypes.Implicits._
import liwec.htmlDsl._
import liwec._

case class Task(val text: String, val completed: Boolean = false)

class TaskComponent(var task: Task, val onDelete: () => Unit)
        extends Component {
    def render() = {
        li(onclick:={_ => task = task.copy(completed = !task.completed)},
            span(task.text),
            span(if(task.completed) img(alt:="C") else Seq()),
            span("X", onclick:={_ => onDelete()})
        )
    }
}

class TodoDemo(var tasks: js.Array[Task]) extends Component {
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
