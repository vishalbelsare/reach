package org.clulab.reach.export.apis.open

import org.clulab.odin._
import org.clulab.processors.{ Document, ProcessorAnnotator }
import org.clulab.reach.ProcessorAnnotatorFactory

import scala.util.Try

/**
  * Create a new Open Domain system engine.
  *   Last Modified: Refactor processor client to processor annotator.
  */
class OpenSystem (processorAnnotator: Option[ProcessorAnnotator] = None) {

  // Get desired processor annotator
  val procAnnotator = processorAnnotator.getOrElse(ProcessorAnnotatorFactory())

  // For the demo, Ruler will provide us with our rules
  var cachedRules: String = ""
  val actions = new OpenActions
  var engine: ExtractorEngine = null

  def mkDoc (text: String): Document = {
    procAnnotator.annotate(text)
  }

  def extractFrom (rules: String, doc: Document): Try[Seq[Mention]] = {
    Try {
      if (rules != cachedRules && rules.nonEmpty) {
        cachedRules = rules
        // We might encounter a rule syntax problem on compilation
        engine = ExtractorEngine(cachedRules, actions)
      }
      engine.extractFrom(doc)
    }
  }

}
