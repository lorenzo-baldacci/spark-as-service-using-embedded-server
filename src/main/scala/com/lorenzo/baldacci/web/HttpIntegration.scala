package com.lorenzo.baldacci.web

import java.io.File

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
  private val temporaryKaggleFileName = AppConfig.kaggleTemporaryFolder + AppConfig.kaggleFileName
  private val sparkSearchEngine = new SparkSearchEngine
  private val storageFolder = AppConfig.storageFolder
  private val paperFolder = AppConfig.papersFolder

  def getHome = HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Hello World!! This is Akka responding</h1>" +
    s"<p>Spark version: ${sparkSearchEngine.getSparkVersion}</p>")

  def addPapersFromFile(): String = {
    if (fileExists(paperFileName)) {
      val papers = readFileFromFolder(paperFileName)
      sparkSearchEngine.addToIndex(papers.map(p => Summary(p.paper_id, p.summary)))
      "Papers added to index"
    } else {
      "Ops, paper file not found :("
    }
  }

  def search(word: String): String = {
    s"[${sparkSearchEngine.getPaperIds(word).mkString(", ")}]"
  }

  def storeIndex: String = {
    if (fileExists(storageFolder)) {
      val index = sparkSearchEngine.getIndex
      writeToLocalFile(index, storageFileName)
      "Index persisted to storage"
    } else {
      "Ops, the storage folder does not exists :("
    }
  }

  def createOrReplaceIndexWithStorage: String = {
    if (fileExists(storageFileName)) {
      sparkSearchEngine.emptyIndex()
      sparkSearchEngine.replaceIndex(retrieveIndex(storageFileName))
      "Index replaced with stored"
    } else {
      "Could not find a previously persisted index. Maintaining the current index."
    }
  }

  def addToIndex(idAndAbstract: String): String = {
    val components: Array[String] = idAndAbstract.split('|')
    if (components.length == 2) {
      val newPaper = Summary(Option(components(0)), Option(components(1)))
      sparkSearchEngine.addToIndex(newPaper)
      "Paper added to index"
    } else {
      "Bad paper format :("
    }
  }

  def refreshIndexFromPublicDataset: String = {
    new File(paperFileName).delete

    if (fileExists(paperFolder))
      downloadAndValidateKaggleFile(temporaryKaggleFileName, paperFileName)
    else
      return "Invalid paper folder provided, refresh operation aborted :("

    if (fileExists(paperFileName)) {
      sparkSearchEngine.emptyIndex()
      val papers = readFileFromFolder(paperFileName)
      sparkSearchEngine.addToIndex(papers.map(p => Summary(p.paper_id, p.summary)))
      "Papers retrieved from remote location and added to index"
    } else {
      "Could not download papers from remote location, refresh aborted :("
    }
  }
}
