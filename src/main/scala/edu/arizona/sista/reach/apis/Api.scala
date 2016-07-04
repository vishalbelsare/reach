package edu.arizona.sista.reach.apis

import java.util.{List => JList}

import com.typesafe.config.ConfigFactory
import edu.arizona.sista.reach._
import edu.arizona.sista.reach.mentions._
import ai.lum.nxmlreader.NxmlReader

import scala.collection.JavaConverters._

/**
  * External interface class to accept and process text strings and NXML documents,
  * returning Reach results as a sequence of BioMentions.
  *   Author: Tom Hicks. 10/19/2015.
  *   Last Modified: Initial creation, after api ruler class.
  */
object Api {
  // Reach results for Scala consumption are a sequence of BioMentions
  type ReachResults = Seq[BioMention]

  // Reach results for Java consumption are a java.util.List of BioMentions
  type JReachResults = JList[BioMention]

  // some internal defaults for parameters required in lower layers
  private val NoSec = "NoSection"
  private val Prefix = "api"
  private val Suffix = "Reach"

  // read configuration to determine processing parameters
  val config = ConfigFactory.load()
  val ignoreSections = config.getStringList("nxml2fries.ignoreSections").asScala.toList
  val encoding = config.getString("encoding")

  val reader = NxmlReader// FIXME (ignoreSections)

  val reach = new ReachSystem               // start reach system

  //
  // Scala API
  //

  /** Extracts raw text from given nxml string and returns Reach results. */
  def runOnNxml (nxml: String): ReachResults = {
    val nxmlDoc = reader.parse(nxml)
    reach.extractFrom(nxmlDoc)
  }

  /** Annotates text by converting it to a FriesEntry and calling runOnFriesEntry(). */
  def runOnText (text: String, docId: String=Prefix, chunkId: String=Suffix): ReachResults = {
    reach.extractFrom(text, docId, chunkId)
  }


  //
  // Java API
  //

  /** Extracts raw text from given nxml string and returns Java Reach results. */
  def runOnNxmlToJava (nxml: String): JReachResults = {
    runOnNxml(nxml).asJava
  }

  /** Annotates text by converting it to a FriesEntry and calling
      runOnFriesEntryToJava(). Uses fake document ID and chunk ID. */
  def runOnTextToJava (text: String): JReachResults = {
    runOnText(text).asJava
  }

  /** Annotates text by converting it to a FriesEntry and calling
      runOnFriesEntryToJava(). */
  def runOnTextToJava (text: String, docId: String): JReachResults = {
    runOnText(text, docId).asJava
  }

  /** Annotates text by converting it to a FriesEntry and calling
      runOnFriesEntryToJava(). */
  def runOnTextToJava (text: String, docId: String, chunkId: String): JReachResults = {
    runOnText(text, docId, chunkId).asJava
  }

}
