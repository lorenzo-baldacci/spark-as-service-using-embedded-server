package com.lorenzo.baldacci.search

import java.io.{BufferedWriter, FileWriter}

import com.lorenzo.baldacci.spark.SparkFactory
import com.lorenzo.baldacci.util.AppConfig
import org.apache.spark.sql.{Dataset, SparkSession}

object SparkSearchEngine {

  implicit private val sparkSession: SparkSession = SparkFactory.spark

  import sparkSession.implicits._

  private val storageFileName = AppConfig.appStorageFolder + AppConfig.appStorageFileName
  private val paperFolder = AppConfig.appPapersFolder

  def getSparkVersion: String = sparkSession.version

  def getIndexedFileLines: Long = {
    val csvFile = "Users/lbaldacci/Develop/data/iclr2017_papers.csv"
    val fileData = sparkSession.read.textFile(s"file:///$csvFile")
    fileData.count
  }

  def readFilesFromFolder(folder: String): Dataset[Paper] = {
    val csvFile = AppConfig.appPapersFolder + "iclr2017_papers.csv"
    sparkSession.read.format("csv")
      .option("sep", ",")
      .option("header", "true")
      .load(s"file://$csvFile")
      .withColumnRenamed("abstract", "summary")
      .withColumnRenamed("tl;dr", "tldr")
      .as[Paper]
  }

  def writeToLocalFile(ds: Dataset[InverseIndex], fullFileName: String): Unit = {
    val records: Array[InverseIndex] = ds.collect()

    val outputFile = new BufferedWriter(new FileWriter(fullFileName))
    outputFile.write("key,paperId\n")
    records.foreach(r => outputFile.write(s"${r.key}, ${r.paperId}\n"))
    outputFile.close()
  }

  def createOrReplaceIndex: Long = {
    val papers = readFilesFromFolder(paperFolder)

    val invertedIndexProcessor = new InvertedIndexProcessor
    val invertedIndex = invertedIndexProcessor.CreateInvertedIndex(papers.map(p => Item(p.paper_id, p.summary)))

    writeToLocalFile(invertedIndex, storageFileName)

    papers.count
  }

  def retrievePaperIds(word: String): Array[String] = {
    val index = sparkSession.read.format("csv")
      .option("sep", ",")
      .option("header", "true")
      .load(s"file://$storageFileName")
      .as[InverseIndex]

    index.filter(_.key == word).map(_.paperId).collect()
  }

}

case class Paper(summary: Option[String], authorids: Option[String], authors: Option[String], conflicts: Option[String],
                 keywords: Option[String], paper_id: Option[String], paperhash: Option[String], title: Option[String],
                 tldr: Option[String], decision: Option[String], forum_link: Option[String], pdf_link: Option[String])

