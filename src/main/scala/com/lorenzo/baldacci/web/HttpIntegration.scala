package com.lorenzo.baldacci.web

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import com.lorenzo.baldacci.search.{SparkSearchEngine, Summary}
import com.lorenzo.baldacci.util.{AppConfig, SparkFactory}
import com.lorenzo.baldacci.util.IOLayer._
import org.apache.spark.sql.SparkSession

object HttpIntegration {
  implicit val sparkSession: SparkSession = SparkFactory.spark
  import sparkSession.implicits._

  private val storageFileName = AppConfig.storageFolder + AppConfig.storageFileName
  private val paperFileName = AppConfig.papersFolder + AppConfig.kaggleFileName
  private val temporaryKaggleFileName = AppConfig.temporaryKaggleFolder + AppConfig.kaggleFileName
  private val sparkSearchEngine = new SparkSearchEngine

  def getHome = HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Hello World!! This is Akka responding</h1>" +
    s"<p>Spark version: ${sparkSearchEngine.getSparkVersion}</p>")

  def addPapersFromFile(): String = {
    val papers = readFilesFromFolder(paperFileName)
    sparkSearchEngine.addToIndex(papers.map(p => Summary(p.paper_id, p.summary)))
    "Papers added to index"
  }

  def search(word: String): String = {
    s"[${sparkSearchEngine.getPaperIds(word).mkString(", ")}]"
  }

  def storeIndex: String = {
    val index = sparkSearchEngine.getIndex
    writeToLocalFile(index, storageFileName)
    "Index persisted to storage"
  }

  def createOrReplaceIndexWithStorage: String = {
    sparkSearchEngine.emptyIndex()
    sparkSearchEngine.replaceIndex(retrieveIndex(storageFileName))
    "Index replaced with stored"
  }

  def addToIndex(idAndAbstract: String): String = {
    val components: Array[String] = idAndAbstract.split('|')
    val newPaper = Summary(Option(components(0)), Option(components(1)))
    sparkSearchEngine.addToIndex(newPaper)
    "Paper added to index"
  }

  def test: String = {
    downloadAndValidateKaggleFile(temporaryKaggleFileName, paperFileName)
  }
}
