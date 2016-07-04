package edu.arizona.sista.reach

import java.io._
import edu.arizona.sista.reach.context.ContextEngineFactory.Engine
import scala.collection.JavaConverters._
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FilenameUtils
import edu.arizona.sista.odin._
import edu.arizona.sista.reach.utils.DSVParser
import edu.arizona.sista.reach.mentions._
import edu.arizona.sista.utils.Serializer
import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.mutable.ParArray
import ai.lum.nxmlreader.NxmlReader


object PaperReader {

  type PaperId = String
  type Dataset = Map[PaperId, Vector[Mention]]

  println("loading ...")
  val config = ConfigFactory.load()
  // the number of threads to use for parallelization
  val threadLimit = config.getInt("threadLimit")
  val ignoreSections = config.getStringList("nxml2fries.ignoreSections").asScala

  // systems for reading papers
  val nxmlReader = NxmlReader//FIXME(ignoreSections)
  val dsvReader = new DSVParser()

  // for context engine
  val contextEngineType = Engine.withName(config.getString("contextEngine.type"))
  val contextConfig = config.getConfig("contextEngine.params").root
  val contextEngineParams: Map[String, String] =
    context.createContextEngineParams(contextConfig)

  // initialize ReachSystem with appropriate context engine
  val rs = new ReachSystem(contextEngineType = contextEngineType, contextParams = contextEngineParams)

  /**
   * Produces Dataset from a directory of nxml and csv papers
   * @param path a directory of nxml and csv papers
   * @return a Dataset (PaperID -> Mentions)
   */
  def readPapers(path: String): Dataset = readPapers(new File(path))

  /**
   * Produces Dataset from a directory of nxml and csv papers.
   * @param dir a File (directory) of nxml and csv papers
   * @return a Dataset (PaperID -> Mentions)
   */
  def readPapers(dir: File): Dataset = {
    require(dir.isDirectory, s"'${dir.getCanonicalPath}' is not a directory")
    // read papers in parallel
    val files = dir.listFiles.par
    // limit parallelization
    files.tasksupport =
      new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(threadLimit))
    // build dataset
    val data: ParArray[(String, Vector[Mention])] = for {
      file <- dir.listFiles.par // read papers in parallel
      // allow either nxml or csv files
      if file.getName.endsWith(".nxml") || file.getName.endsWith(".csv")
    } yield readPaper(file)
    data.seq.toMap
  }

  /**
   * Produces Mentions from either a .nxml or .csv paper using [[NxmlReader]] or [[DSVParser]] and [[ReachSystem]]
   * @param file a File with either the .csv or .nxml extension
   * @return (PaperID, Mentions)
   */
  def readPaper(file: File): (String, Vector[Mention]) = {
    require(file.getName.endsWith(".nxml"), s"Given ${file.getAbsolutePath}, but readNXMLPaper only handles .nxml files!")
    val paperID = FilenameUtils.removeExtension(file.getName)
    println(s"reading paper $paperID . . .")
    paperID -> rs.extractFrom(nxmlReader.read(file)).toVector
  }

  /**
   * Get mentions from text
   */
  def getMentionsFromText(text: String): Seq[Mention] = rs.extractFrom(text, "", "")

}


object ReadPapers extends App {

  val config = ConfigFactory.load()

  val papersDir = config.getString("ReadPapers.papersDir")
  val outFile = config.getString("ReadPapers.serializedPapers")

  println("reading papers ...")
  val dataset = PaperReader.readPapers(papersDir)

  println("serializing ...")
  Serializer.save(dataset, outFile)
}
