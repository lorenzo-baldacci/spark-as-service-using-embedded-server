package com.lorenzo.baldacci.util

import java.io.{BufferedWriter, FileWriter}
import sys.process._
import com.lorenzo.baldacci.search.{InverseIndex, Paper}
import org.apache.spark.sql.{Dataset, SparkSession}

object IOLayer {
  def fileExists(fileFullName: String) = new java.io.File(fileFullName).exists

  def readFileFromFolder(fileFullName: String)(implicit sparkSession: SparkSession): Dataset[Paper] = {
    import sparkSession.implicits._

    sparkSession.read.format("csv")
      .option("sep", ",")
      .option("header", "true")
      .load(s"file://$fileFullName")
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

  def retrieveIndex(storageFileName: String)(implicit sparkSession: SparkSession): Dataset[InverseIndex] = {
    import sparkSession.implicits._

    SparkFactory.spark.read.format("csv")
      .option("sep", ",")
      .option("header", "true")
      .load(s"file://$storageFileName")
      .as[InverseIndex]
  }

  def validKaggleFile(kaggleFileName: String)(implicit sparkSession: SparkSession): Boolean = {
    val fileSchema = SparkFactory.spark.read.format("csv")
      .option("sep", ",")
      .option("header", "true")
      .load(s"file://$kaggleFileName")
      .withColumnRenamed("abstract", "summary")
      .withColumnRenamed("tl;dr", "tldr")
      .schema.map(_.name).toSet
    val expectedSchema = Set("summary", "authorids", "authors", "conflicts", "keywords", "paper_id", "paperhash",
      "title", "tldr", "decision", "forum_link", "pdf_link")

    fileSchema == expectedSchema
  }

  def downloadAndValidateKaggleFile(temporaryFileName: String, targetFileName: String)(implicit sparkSession: SparkSession): Unit = {
    s"${AppConfig.kaggleBin} datasets download -d ${AppConfig.kaggleDataset}".!

    if (validKaggleFile(temporaryFileName))
      s"cp $temporaryFileName $targetFileName".!
  }
}
