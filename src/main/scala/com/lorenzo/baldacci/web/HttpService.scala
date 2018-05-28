package com.lorenzo.baldacci.web

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import com.lorenzo.baldacci.search.SparkSearchEngine

object HttpService {

  def getHome = HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Hello World!! This is Akka responding</h1>" +
    s"<p>Spark version: ${SparkSearchEngine.getSparkVersion}</p>")

  def getFileLines: String = "Number of lines in file: " + SparkSearchEngine.getIndexedFileLines

  def createOrReplaceIndex: String = "Number of lines in file: " + SparkSearchEngine.createOrReplaceIndex

  def getPapers(word: String): String = s"getPapers($word: Int)"

}
