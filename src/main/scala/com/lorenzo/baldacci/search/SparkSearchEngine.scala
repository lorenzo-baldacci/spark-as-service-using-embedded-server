package com.lorenzo.baldacci.search

import java.io.{BufferedWriter, FileWriter}

import com.lorenzo.baldacci.spark.SparkFactory
import com.lorenzo.baldacci.util.AppConfig
import org.apache.spark.sql.{Dataset, SparkSession}

object SparkSearchEngine {

  implicit private val sparkSession: SparkSession = SparkFactory.spark

  import sparkSession.implicits._

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
//    val records: Array[InverseIndex] = ds.collect()

//    records.foreach(println)

//    val outputFile = new BufferedWriter(new FileWriter(fullFileName))
//    outputFile.write("key,paperId")
//    records.foreach(r => outputFile.write(s"${r.key}, ${r.paperId}"))
//    outputFile.close()
  }

  def createOrReplaceIndex: Long = {
    val papers = readFilesFromFolder(AppConfig.appPapersFolder)

    val invertedIndexProcessor = new InvertedIndexProcessor
    val invertedIndex = invertedIndexProcessor.CreateInvertedIndex(papers.map(p => Item(p.paper_id, p.summary)))

    invertedIndex.show()

    writeToLocalFile(invertedIndex, AppConfig.appStorageFolder + AppConfig.appStorageFileName)

    papers.count
  }
}

case class Paper(summary: String, authorids: String, authors: String, conflicts: String, keywords: String,
                 paper_id: String, paperhash: String, title: String, tldr: String, decision: String,
                 forum_link: String, pdf_link: String)

