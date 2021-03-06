// This file is part of liwec and is licenced under the MPL v2.0
// (c) 2019 David Koňařík

package liwec.htmlCodegen

import java.util.Calendar
import liwec.htmlCodegen.mdnScraper._

class TypeNamer {
    var i = 0;

    def makePascalCase(str: String) =
        "(^|-)(.)".r.replaceAllIn(str,
            m => m.group(1) + m.group(2).toUpperCase)

    def attrGroupTypeName(applicableEls: Option[Seq[String]]) =
        applicableEls match {
            case None => "ElementVNode"
            case Some(Seq(tag)) => s"VNodeTag${makePascalCase(tag)}"
            case _ => { i += 1; s"AttrApplicable$i" }
        }

    def elTypeName(el: ElementDoc) = s"VNodeTag${makePascalCase(el.name)}"

}

object Codegen {
    val symRenames = Map(
        "class" -> "cls",
        "type" -> "typeAttr",
        "for" -> "forAttr",
        "type" -> "typeAttr",
        "for" -> "forAttr",
        "object" -> "objectAttr",
        "var" -> "varAttr",
        "scoped" -> "scopedAttr",
    )

    def renamedSym(sym: String) = {
        val ren = symRenames.getOrElse(sym, sym)
        "-(.)".r.replaceAllIn(ren, _.group(1).toUpperCase)
    }

    def generateOutput(els: Seq[ElementDoc], attrs: Seq[AttributeDoc],
                       events: Seq[EventDoc]) = {
        val typeNamer = new TypeNamer()

        val attrsByEls = attrs.groupBy(_.elNames)
        val agts =
            attrsByEls
            .map {case (ae, as) => (as, typeNamer.attrGroupTypeName(ae)) }
        val agtsByAttr =
            agts.flatMap { case (as, agt) => as.map((_, agt)) }
        val attrGroupTypes =
            agtsByAttr.values.toSet
            .filter(n => n != "ElementVNode" && ! n.matches("^VNodeTag.*"))
        val elsWithType =
            els.map(el => (el, typeNamer.elTypeName(el)))
        val elTypesWithExtends =
            for((el, elType) <- elsWithType) yield {
                val agts =
                    agtsByAttr
                    .filter(_._1.elNames.getOrElse(Seq()) contains el.name)
                    .map(_._2)
                    .filter(!_.matches("^VNodeTag.*"))
                    .toSet
                (elType, agts)
            }
        val elNames =
            els.map(_.name).toSet

        val timestamp = Calendar.getInstance().getTime()
        def attrName(name: String) = {
            val name1 = renamedSym(name)
            if(elNames contains name1) name1 + "Attr" else name1
        }

        // Sorry for the ugly "template"
s"""
/*
 * Generated by liwec codegen on ${timestamp}
 */

package liwec

import scalajs.js
import org.scalajs.dom._
import liwec.domvm._

package object htmlDsl
        extends Implicits {
    // Unfortunately, this has to be here, as there can't be multiple
    // package objects with the same name and type aliases aren't allowed
    // in regular packages.
    type VNodeFrag = VNodeApplicable[ElementVNode]

    // Event types that aren't in scalajs-dom
    type MutationNameEvent = Event
    type SpeechSynthesisEvent = Event
    type GamepadEvent = Event
    type TimeEvent = Event
    type NotificationEvent = Event
    type SVGEvent = Event
    type AudioProcessingEvent = Event
    type DeviceMotionEvent = Event
    type SpeechRecognitionEvent = Event
    type PushEvent = Event
    type SVGZoomEvent = Event
    type UserProximityEvent = Event
    type DeviceProximityEvent = Event
    type DeviceOrientationEvent = Event
    type PageTransitionEvent = Event
    type DeviceLightEvent = Event

""" +
    (for(attrGroupType <- attrGroupTypes) yield
        s"""    trait $attrGroupType extends ElementVNode""").mkString("\n") +
"\n" +
    (for((attr, agt) <- agtsByAttr) yield
        s"""
    /** ${attr.description} */
    lazy val ${attrName(attr.name)} = Attr[${agt}, String]("${attr.name}")""")
    .mkString("") +
"\n" +
    (for(ev <- events) yield
        s"""
    lazy val ${attrName("on-" + ev.name)} = """ +
        s"""Attr[ElementVNode, js.Function1[${ev.jsType}, Unit]]""" +
        s"""("on${ev.name}")""")
    .mkString("") +
"\n" +
    (for((elType, exts) <- elTypesWithExtends) yield
        s"""
    trait $elType extends ElementVNode """ +
        (if(exts.size > 0) " with " + exts.mkString(" with ") else "")
    ).mkString("") +
"\n" +
    (for((el, elType) <- elsWithType) yield {
        val name = renamedSym(el.name)
        s"""
    /** ${el.description} */
    lazy val $name = Tag[$elType]("${el.name}")"""}).mkString("") +
s"""
}
"""
    }

    def main(args: Array[String]): Unit = {
        val els = getElementList().toSeq
        val attrs = getAttributeList().toSeq
        val events = getEventList().toSeq
        val out = generateOutput(els, attrs, events)
        println(out)
    }
}
