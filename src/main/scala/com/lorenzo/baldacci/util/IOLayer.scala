package com.lorenzo.baldacci.util

import java.io.{BufferedWriter, FileWriter}

import com.lorenzo.baldacci.search.{InverseIndex, Paper}
import org.apache.spark.sql.{Dataset, SparkSession}

object IOLayer {
  def readFilesFromFolder(folder: String)(implicit sparkSession: SparkSession): Dataset[Paper] = {
    import sparkSession.implicits._

    val csvFile = AppConfig.papersFolder + "iclr2017_papers.csv"
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

  def retrieveIndex(storageFileName: String)(implicit sparkSession: SparkSession): Dataset[InverseIndex] = {
    import sparkSession.implicits._

    SparkFactory.spark.read.format("csv")
      .option("sep", ",")
      .option("header", "true")
      .load(s"file://$storageFileName")
      .as[InverseIndex]
  }
}
