package org.clulab.reach.grounding

import org.clulab.reach.grounding.ReachIMKBMentionLookups._

/**
  * Object implementing logic to enumerate context related KB entries.
  *   Written by Tom Hicks. 2/19/2016.
  *   Last Modified: Update for hiding of KB entry class.
  */
object ReachContextKBLister {
  /** A sequence of the context related KB instances, whose values are to be listed. */
  val ContextKBs = Seq(
    (ContextCellLine, "CellLine"),
    (ContextCellLine2, "CellLine"),
    (ContextCellType, "CellType"),
    (ContextSpecies, "Species"),
    (ContextTissueType, "TissueType"),
    (ContextOrgan, "Organ"),
    (StaticCellLocation, "Cellular_component"),
    (StaticCellLocation2, "Cellular_component"),
    (ModelGendCellLocation, "Cellular_component")
  )

  /** Return a sequence of grounding information objects from the context related KBs. */
  def listContextKBs: Seq[ContextGrounding] = ContextKBs.flatMap {
    case (kb, ctxType) =>
      kb.resolutions.map(_.map(kbr =>
        ContextGrounding(ctxType, kbr.text, kbr.namespace, kbr.id, kbr.nsId, kbr.species) ))
  }.flatten

  /** Case class to hold grounding information about context related KB entries. */
  case class ContextGrounding(ctxType:String, text:String, namespace:String, id:String,
                              nsId:String, species:String)

}
