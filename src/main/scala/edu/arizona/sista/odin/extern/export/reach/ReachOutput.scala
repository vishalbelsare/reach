package edu.arizona.sista.odin.extern.export.reach

import java.io._
import java.util.Date
import edu.arizona.sista.bionlp.FriesEntry
import edu.arizona.sista.utils.DateUtils

import edu.arizona.sista.processors._
import edu.arizona.sista.odin._
import edu.arizona.sista.bionlp.mentions._
import edu.arizona.sista.odin.extern.export.{IncrementingId, JsonOutputter}
import edu.arizona.sista.odin.extern.export.JsonOutputter._

/**
  * Defines classes and methods used to build and output REACH models.
  *   Written by Tom Hicks. 5/7/2015.
  *   Last Modified: Update for json outputter rename to Fries.
  */
class ReachOutput extends JsonOutputter {
  type IDed = scala.collection.mutable.HashMap[Mention, String]
  type FrameList = scala.collection.mutable.MutableList[PropMap]  // has O(c) append

  // Constants:
  val AssumedProteins = Set("Family", "Gene_or_gene_product", "Protein")

  // incrementing ID for numbering entities
  protected val idCntr = new IncrementingId()

  // create mention manager and cache
  protected val mentionMgr = new MentionManager()


  //
  // Public API:
  //

  /**
    * Returns the given mentions in the REACH JSON format, as one big string.
    */
  override def toJSON (paperId:String,
                       allMentions:Seq[Mention],
                       paperPassages:Seq[FriesEntry],
                       startTime:Date,
                       endTime:Date,
                       outFilePrefix:String): String = {
    val model:PropMap = new PropMap
    val mentions = allMentions.filter(allowableRootMentions)
    val mIds = assignMentionIds(mentions, new IDed)
    val frames = new FrameList
    mentions.foreach { mention =>
      val frame = beginNewFrame(mention, startTime, endTime, mIds)
      frames += doMention(mention, mIds, frame)
    }
    model("frames") = frames
    return writeJsonToString(model)
  }


  /**
    * Writes the given mentions to an output file in REACH JSON format.
    * The output file is prefixed with the given prefix string.
    */
  override def writeJSON (paperId:String,
                          allMentions:Seq[Mention],
                          paperPassages:Seq[FriesEntry],
                          startTime:Date,
                          endTime:Date,
                          outFilePrefix:String) = {
    val model:PropMap = new PropMap
    val mentions = allMentions.filter(allowableRootMentions)
    val mIds = assignMentionIds(mentions, new IDed)
    val frames = new FrameList
    mentions.foreach { mention =>
      val frame = beginNewFrame(mention, startTime, endTime, mIds)
      frames += doMention(mention, mIds, frame)
    }
    model("frames") = frames
    val outFile = new File(outFilePrefix + ".json")
    writeJsonToFile(model, outFile)
  }


  //
  // Private Methods
  //

  /** Return true if the given mention is one that should be processed if it is an argument. */
  private def allowableArgumentMentions (mention:Mention): Boolean = {
    return (mention.isInstanceOf[EventMention] || mention.isInstanceOf[RelationMention])
  }

  /** Return true if the given mention is one that should be processed at the forest root. */
  private def allowableRootMentions (mention:Mention): Boolean = {
    return (mention.isInstanceOf[EventMention] || mention.isInstanceOf[RelationMention])
  }

  /** Assign all mentions a unique ID. */
  private def assignMentionIds (mentions:Seq[Mention], mIds:IDed): IDed = {
    mentions.foreach{ mention =>
      mIds.getOrElseUpdate(mention, idCntr.genNextId())
      assignMentionIds(mention.arguments.values.toSeq.flatten.filter(allowableArgumentMentions), mIds)
    }
    return mIds
  }

  /** Return a new index frame (map) initialized with the (repeated) document information. */
  private def beginNewFrame (mention:Mention, startTime:Date, endTime:Date, mIds:IDed): PropMap = {
    val doc:Document = mention.document
    val frame = new PropMap
    val docSecId = doc.id.getOrElse("DOC-ID_MISSING").split("_").map(_.trim)
    frame("doc_id") = docSecId(0)
    frame("passage_id") = docSecId(1)
    frame("event_id") = mIds.get(mention)
    frame("reading_started") = DateUtils.formatUTC(startTime)
    frame("reading_ended") = DateUtils.formatUTC(endTime)
    frame("submitter") = "UAZ"
    frame("reader_type") = "machine"
    frame("text") = mention.text
    frame("found_by") = mention.foundBy
    frame("offsets") = List(mention.startOffset, mention.endOffset)
    if (mentionMgr.isNegated(mention.toBioMention)) frame("negated") = "true"
    if (mentionMgr.isHypothesized(mention.toBioMention)) frame("hypothesized") = "true"
    // TODO: ?? sentence, start-pos, end-pos, verbose-text from FriesOutput ??
    return frame
  }

  /** Dispatch on and process the given mention, returning its information in a properties map. */
  private def doMention (mention:Mention, mIds:IDed, frame:PropMap): PropMap = {
    mention.label match {
      case "Binding" => doBinding(mention, frame)
      case "Negative_activation" => doRegulation(mention, frame, mIds, false)
      case "Negative_regulation" => doRegulation(mention, frame, mIds, false)
      case "Positive_activation" => doRegulation(mention, frame, mIds)
      case "Positive_regulation" => doRegulation(mention, frame, mIds)
      case "Translocation" => doTranslocation(mention, frame)
      case _ => doSimpleType(mention, frame)  // handle all like phosphorylation...
      // TODO: handle Degradation, Exchange, Expression, Translation, Transcription
    }
  }

  /** Return properties map for the given binding mention. */
  private def doBinding (mention:Mention, frame:PropMap): PropMap = {
    val themeArgs = mentionMgr.themeArgs(mention)
    if (themeArgs.isDefined) {
      frame("type") = "complex_assembly"
      val themes = themeArgs.get
      frame("participants") =
        if (themes.size == 0) null
        else themes.map(doTextBoundMention(_))
      // TODO: binding sites
      // val sites = themeArgs.get.map(getSite(_))
    }
    return frame
  }

  /** Return properties map for the given positive regulation mention. */
  private def doRegulation (mention:Mention, frame:PropMap, mIds:IDed, positive:Boolean=true): PropMap =
  {
    val controllerArgs = mentionMgr.controllerArgs(mention)
    val controlledArgs = mentionMgr.controlledArgs(mention)
    if (controllerArgs.isDefined && controlledArgs.isDefined) {
      frame("type") = mention.label.toLowerCase
      val controller = controllerArgs.get.head
      // TODO: controllers can now be relation mentions:
      frame("controller") = doTextBoundMention(controller) // CHANGE LATER
      frame("controlled") = mIds.get(controlledArgs.get.head)
    }
    return frame
  }

  /** Return properties map for the given phosphorylation mention. */
  private def doSimpleType (mention:Mention, frame:PropMap): PropMap = {
    val themeArgs = mentionMgr.themeArgs(mention)
    if (themeArgs.isDefined) {
      frame("type") = mention.label.toLowerCase
      val themes = themeArgs.get.map(doTextBoundMention(_))
      frame("participants") = if (themes.size == 0) null else themes

      val site = getSite(mention)
      if (site.isDefined) {
        val subFlds = new PropMap
        subFlds("site") = site.orNull
        frame("subfields") = subFlds
      }
    }
    return frame
  }

  /** Return a properties map for the given single text bound mention. */
  private def doTextBoundMention (mention:Mention, context:String="protein"): PropMap = {
    val part = new PropMap
    part("type") = if (AssumedProteins.contains(mention.label)) context
                   else mention.label.toLowerCase
    part("text") = mention.text
    part("id") = mention.toBioMention.xref.map(_.id).orNull
    part("namespace") = mention.toBioMention.xref.map(_.namespace).orNull
    return part
  }

  /** Return properties map for the given translocation mention. */
  private def doTranslocation (mention:Mention, frame:PropMap): PropMap = {
    val themeArgs = mentionMgr.themeArgs(mention)
    if (themeArgs.isDefined) {
      frame("type") = mention.label.toLowerCase
      val themes = themeArgs.get.map(doTextBoundMention(_))
      frame("participants") = if (themes.size == 0) null else themes

      val from = getSource(mention)
      val to = getDestination(mention)
      if (from.isDefined && to.isDefined) {
        val subFlds = new PropMap
        subFlds("from") = from.orNull
        subFlds("to") = to.orNull
        frame("subfields") = subFlds
      }
    }
    return frame
  }

  /** Process optional destination argument on the given mention, returning a properties map. */
  private def getDestination (mention:Mention): Option[PropMap] = {
    val destArgs = mentionMgr.destinationArgs(mention)
    return destArgs.map(osm => doTextBoundMention(osm.head))
  }

  /** Process the given mention argument, returning a ns:id string option for the first arg. */
  private def getId (args:Option[Seq[Mention]]): Option[String] = {
    if (args.isDefined)
      return args.get.head.toBioMention.xref.map(_.id)
    else return None
  }

  /** Process optional site argument on the given mention, returning a site string option. */
  private def getSite (mention:Mention): Option[String] = {
    return getText(mentionMgr.siteArgs(mention))
  }

  /** Process optional source argument on the given mention, returning a properties map. */
  private def getSource (mention:Mention): Option[PropMap] = {
    val sourceArgs = mentionMgr.sourceArgs(mention)
    return sourceArgs.map(osm => doTextBoundMention(osm.head))
  }

  /** Process the given mention argument, returning a text string option for the first arg. */
  private def getText (args:Option[Seq[Mention]]): Option[String] = {
    return if (args.isDefined) Some(args.get.head.text) else None
  }

}

