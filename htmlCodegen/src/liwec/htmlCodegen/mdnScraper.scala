// This file is part of liwec and is licenced under the MPL v2.0
// (c) 2019 David Koňařík

package liwec.htmlCodegen

import scala.annotation.tailrec
import org.jsoup._
import org.jsoup.nodes._
import com.themillhousegroup.scoup.ScoupImplicits._

package object mdnScraper {
    case class ElementDoc(name: String, description: String, empty: Boolean)
    case class AttributeDoc(
        name: String, description: String, elNames: Option[Seq[String]])
    case class EventDoc(name: String, jsType: String, description: String)

    val mdnRootUrl = "https://developer.mozilla.org"
    val mdnEmptyElementListUrl =
        s"$mdnRootUrl/en-US/docs/Glossary/empty_element"
    val mdnElementListUrl = s"$mdnRootUrl/en-US/docs/Web/HTML/Element"
    val mdnAttributeListUrl = s"$mdnRootUrl/en-US/docs/Web/HTML/Attributes"
    val mdnEventListUrl = s"$mdnRootUrl/en-US/docs/Web/Events"

    implicit class BetterList[T](val l: List[T]) extends AnyVal {
        /* Order *not* guaranteed! */
        def splitOn(pred: (T) => Boolean) = {
            @tailrec
            def inner(xs: List[T], res: List[List[T]], acc: List[T]):
                    List[List[T]] =
                xs match {
                    case Nil => acc :: res
                    case head :: tail =>
                        if(pred(head)) inner(tail, acc :: res, head :: Nil)
                        else inner(tail, res, head :: acc)
                }
            inner(l, Nil, Nil)
        }
    }

    def hasTag(tag: String)(el: Element) = el.tagName() == tag
    def articleSections(article: Element) =
        article.children.toList.splitOn(hasTag("h2") _)

    def elLinkToElName(elLink: Element) = elLink.attr("href").split("/").last
    def getEmptyElementList() = {
        val doc = Jsoup.connect(mdnEmptyElementListUrl).get()
        val list = doc.selectFirst("#wikiArticle ul")
        for(elLink <- list.select("a")) yield elLinkToElName(elLink)
    }

    def getElementList() = {
        val emptyElements = getEmptyElementList().toList

        val doc = Jsoup.connect(mdnElementListUrl).get()
        val parts = articleSections(doc.selectFirst("#wikiArticle"))
        (for(part <- parts;
            h2 <- part.find(hasTag("h2") _);
            table <- part.find(hasTag("table"))) yield {
            for(row <- table.select("tbody tr")) yield {
                val elLink = row.children.get(0).selectFirst("a")
                val elName = elLinkToElName(elLink)
                val description = row.children.get(1).text()
                val empty = emptyElements contains elName
                ElementDoc(elName, description, empty)
            }
        })
        .flatten
        .groupBy(_.name)
        .map(_._2.head)
    }

    def getAttributeList() = {
        val doc = Jsoup.connect(mdnAttributeListUrl).get()
        val table = doc.selectFirst("#wikiArticle table")
        (for(row <- table.select("tbody tr")) yield {
            val attrName = row.children().get(0).text()
            val description = row.children().get(2).text()
            val elCol = row.children().get(1)
            val elNames =
                if(elCol.text().trim() == "Global attribute") None
                else Some (elCol.select("a").map(elLinkToElName(_)).toSeq)
            AttributeDoc(attrName, description, elNames)
        })
        .filter(_.name != "data-*")
    }

    /** Returns only standard events */
    def getEventList() = {
        val doc = Jsoup.connect(mdnEventListUrl).get()
        val sections = articleSections(doc.selectFirst("#wikiArticle"))
        val stdEvsTable =
            sections.find(s =>
                s.find(hasTag("h2") _)
                .map(_.text() == "Standard events")
                .getOrElse(false))
            .head
            .map(_.select("table"))
            .find(_.size > 0)
            .head
        (for(row <- stdEvsTable.select("tbody tr")) yield {
            val evName = row.children().get(0).text().trim()
            val evJsType1 = row.children().get(1).text()
            val evJsType =
                if((evJsType1 contains " ") || evJsType1 == "") "Event"
                else evJsType1
            val description = row.children().get(3).text()
            EventDoc(evName, evJsType, description)
        })
        .groupBy(_.name)
        .map { case (n, es) =>
            if(es.size > 1) es.head.copy(jsType = "Event") else es.head }
    }
}
