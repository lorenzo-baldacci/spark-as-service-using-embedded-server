package com.lorenzo.baldacci.search

import org.apache.spark.sql.{Dataset, SparkSession}

class SparkSearchEngine(implicit sparkSession: SparkSession) {
  import sparkSession.implicits._

  private var index: Dataset[InverseIndex] = sparkSession.createDataset(Seq.empty[InverseIndex])

  def getSparkVersion: String = sparkSession.version

  def replaceIndex(newIndex: Dataset[InverseIndex]): Unit = index = newIndex.cache()

  def emptyIndex(): Unit = index = sparkSession.createDataset(Seq.empty[InverseIndex])

  def getIndex: Dataset[InverseIndex] = index

  def addToIndex(papers: Dataset[Summary]): Unit = {
    val invertedIndexProcessor = new InvertedIndexProcessor
    val indexedPapers = invertedIndexProcessor.CreateInvertedIndex(papers)
    index = index.union(indexedPapers).distinct().cache()
  }

  def addToIndex(paper: Summary): Unit = {
    val invertedIndexProcessor = new InvertedIndexProcessor
    val indexedPaper = invertedIndexProcessor.CreateInvertedIndex(paper)
    index = index.union(indexedPaper).distinct().cache()
  }

  def getPaperIds(word: String): Array[String] = {
    index.filter(_.key == word.toLowerCase()).map(_.paperId).collect()
  }

}

case class Paper(summary: Option[String], authorids: Option[String], authors: Option[String], conflicts: Option[String],
                 keywords: Option[String], paper_id: Option[String], paperhash: Option[String], title: Option[String],
                 tldr: Option[String], decision: Option[String], forum_link: Option[String], pdf_link: Option[String])

case class Summary(paperId: Option[String], text: Option[String])

case class InverseIndex(key: String, paperId: String)