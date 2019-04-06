# Liwec user's guide

Liwec is a Scala library for creating web GUIs. It uses
[Mithril](https://mithril.js.org/) under the Scala abstractions, but that is
an implementation detail, not a user-exposed fact.

Its main design goal is using existing Scala language constructs to let
programmers create web GUIs with clear code and no bolierplate. It separates
the page into discrete and reusable "components", which are represented as
Scala classes. When a component's property is changed a "rerender" is
triggered, which uses the underlying Mithril library to recreate the VDOM and
synchronise it with the browser's own DOM. This allows the programmer to both
think of the DOM as a function of the application's state and to write
components as consise mutable objects.

To allow for easy abstraction and syntax highlighting, VDOM (HTML) nodes are
created by functions.

# Creating components

Components are simply classes that extend `liwec.Component`. They have to
implement a method called `render()`, which will return a VDOM tree
corresponding to the component's current state.

```scala
import liwec._
import liwec.htmlDsl._

class SampleComponent(var name: String) extends Component {
	def render() =
		div(
			label("Your name:"),
			input(type_:="text", onblur:={ name = _.value }),
			span(s"Hello, $name"),
		)
}
```

Whenever a component's instance variables are changed, the component is
rerendered.

# HTML DSL

VDOM trees are constructed using a DSL within Scala. Each HTML element is
represented by a function which takes attributes and children as arguments.
Attributes are applied by using an object representing the attribute and
calling its `:=` method with the desired value.

Some attributes are renamed due to conflicts with Scala keywords. `class` is
named `cls`, all the other ones are renamed by adding an underscore at the end.

```scala
import liwec.htmlDsl._

ul(cls:="sample-list",
	li("This is the HTML DSL"),
	li("There can be", b("Multiple"), "children"),
)
```

# CSS DSL

CSS for the components can be constructed using a Scala DSL. A macro (`css` or
`cssScoped`) is used to process the DSL at compile-time and save it to
`target/css/out/<class name>.css`.

Rules are created from a selector and a set of rules, separated by `->` or
nothing.

Selectors are created by getting properties of dynamic objects `c`,
`i`, `e`, `pc` and `pe`, which represent CSS selectors for classes, IDs,
tag names, pseudoclasses and pseudoelements respectively. Selectors can be
combined by custom operators. `/` is equivalent to whitespace in CSS, `&` to
concatenation without whitespace, `/+` to `+`, `/~` to `~`, `/>` to `>` and
`/||` to `||`.

The rules are actually parameters to a method call. They are made basically the
same way as HTML attributes.

CSS can be scoped, which means every selector will have `[scope=<class name>]`
added to it, which will ensure the rules only ever apply to elements within
the component. The HTML also needs to be scoped, which can be done by wrapping
it in the `scoped` macro from `liwec.htmlMacros`.

```scala
import liwec.cssMacros._

css { import liwec.cssDsl._
	e.h1 (
		fontFamily := "sans-serif",
	)
	(e.table & c.datatable) / e.tr -> (
		backgroundColor := "#abcdef",
		fontSize := "10pt",
	)
}
```
