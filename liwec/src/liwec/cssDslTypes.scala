// This file is part of liwec and is licenced under the MPL v2.0
// (c) 2019 David Koňařík

package liwec

import scala.language.dynamics

package cssDslTypes {
    // XXX: We might want to measure the performance difference between
    // using this many classes and just a handful holding strings, since this
    // "full-AST" approach gives us flexibility we don't really need.
    
    trait Expression {}

    trait Selector {
        def &(sel: Selector) = AndCombinator(this, sel)
        def /(sel: Selector) = DescCombinator(this, sel)
        def /+(sel: Selector) = AdjSiblingCombinator(this, sel)
        def /~(sel: Selector) = GenSiblingCombinator(this, sel)
        def />(sel: Selector) = ChildCombinator(this, sel)
        def /||(sel: Selector) = ColumnCombinator(this, sel)
        def apply(xs: Expression*) = Rule(this, xs)
        def `->`(xs: Expression*) = Rule(this, xs)

        def css: String
    }

    abstract class PrefixSelector(str: String, prefix: String)
            extends Selector {
        override def css = prefix + str
    }

    case class ClassSelector(className: String)
            extends PrefixSelector(className, ".")
    case class IdSelector(id: String) extends PrefixSelector(id, "#")
    case class TagSelector(tagName: String) extends PrefixSelector(tagName, "")
    case class PseudoClassSelector(pseudoclass: String)
            extends PrefixSelector(pseudoclass, ":")
    case class PseudoElementSelector(pseudoel: String)
            extends PrefixSelector(pseudoel, "::")
    case class RawSelector(sel: String) extends Selector {
        override def css = this.sel
    }

    abstract class Combinator(sel1: Selector, sel2: Selector, sep: String)
            extends Selector {
        override def css = s"${this.sel1.css}${this.sep}${this.sel2.css}"
    }

    // XXX: Dillema. Should this have an array of selectors or just two?
    case class AndCombinator(sel1: Selector, sel2: Selector)
            extends Combinator(sel1, sel2, "")
    case class DescCombinator(sel1: Selector, sel2: Selector)
            extends Combinator(sel1, sel2, " ")
    case class AdjSiblingCombinator(sel1: Selector, sel2: Selector)
            extends Combinator(sel1, sel2, " + ")
    case class GenSiblingCombinator(sel1: Selector, sel2: Selector)
            extends Combinator(sel1, sel2, " ~ ")
    case class ChildCombinator(sel1: Selector, sel2: Selector)
            extends Combinator(sel1, sel2, " > ")
    case class ColumnCombinator(sel1: Selector, sel2: Selector)
            extends Combinator(sel1, sel2, " || ")

    class SelectorPrefix(selClass: String => Selector) extends Dynamic {
        def selectDynamic(str: String): Selector = selClass(str)
        def applyDynamic(str: String) = selClass(str)
    }

    trait SelectorPrefixes {
        val c = new SelectorPrefix(ClassSelector)
        val i = new SelectorPrefix(IdSelector)
        val e = new SelectorPrefix(TagSelector)
        val pc = new SelectorPrefix(PseudoClassSelector)
        val pe = new SelectorPrefix(PseudoElementSelector)
    }

    sealed case class Property[T](name: String) {
        def `:=`(value: T) = PropertyValue(this, value)
    }

    sealed case class PropertyValue[T](property: Property[T], value: T)
        extends Expression {
        override def toString() =
            this.property.name + ": " + this.value.toString()
    }

    sealed case class Rule(selector: Selector, children: Seq[Expression])
            extends Expression {
        def toCssRules(parentSelector: Option[Selector] = None
                ): List[CssRule] = {
            val bareProps =
                this.children.collect { case c: PropertyValue[_] => c }
            val innerRules = this.children.collect { case r: Rule => r }
            val sel = parentSelector match {
                case Some(ps) => ps / this.selector
                case None => this.selector
            }
            (List(CssRule(sel, bareProps))
             ++ innerRules.flatMap(_.toCssRules(Some(sel))))
        }
    }

    sealed case class CssRule(
            selector: Selector, props: Seq[PropertyValue[_]]) {
        override def toString() =
            s"${this.selector.css} { ${this.props.mkString("; ")} }"
    }


}

package object cssDslTypes {
    def scopeCss(scopeSel: Selector, sel: Selector): Selector = {
        val s = (sel: Selector) => scopeCss(scopeSel, sel)
        sel match {
            case DescCombinator(s1, s2) => DescCombinator(s(s1), s(s2))
            case AdjSiblingCombinator(s1, s2) =>
                AdjSiblingCombinator(s(s1), s(s2))
            case GenSiblingCombinator(s1, s2) =>
                GenSiblingCombinator(s(s1), s(s2))
            case ChildCombinator(s1, s2) => ChildCombinator(s(s1), s(s2))
            case ColumnCombinator(s1, s2) => ColumnCombinator(s(s1), s(s2))
            case _ => AndCombinator(sel, scopeSel)
        }
    }
}
