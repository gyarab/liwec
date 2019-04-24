package liwec

import org.scalajs.dom

package object routing {
    /** This class deals with "simulating" link-based navigation in an SPA. */
    abstract class Router {
        import dom.window._

        def matchUrl(url: String): Component

        def goToUrl(url: String) =
            // Not using the stored objects is a conscious decision, so that pages
            // can be linked externally.
            history.pushState(null, "", location.origin + "/" + url)

        def currentComponent =
            this.matchUrl(location.pathname)
    }

    implicit class PathExtractor(sc: StringContext) {
        object path {
            def unapplySeq(pathStr: String): Option[Seq[String]] = {
                sc.parts.foldLeft(Option((0, List[String]()))) {
                    (state, part) =>
                        state.flatMap { case (i, params) =>
                            val partEquiv = pathStr.drop(i).take(part.length)
                            if(partEquiv != part) None
                            else {
                                val rest = pathStr.drop(i + part.length)
                                                  .takeWhile(_ != '/')
                                val newI = i + part.length + rest.length
                                val newParams =
                                    if(params.length == sc.parts.length - 1) {
                                        if(i + part.length != pathStr.length)
                                            None
                                        else Some(params)
                                    } else Some(rest :: params)
                                newParams.map(np => (newI, np))
                            }
                        }
                }
                .map(_._2.reverse)
            }
        }
    }
}
