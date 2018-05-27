package com.lorenzo.baldacci.web

import com.lorenzo.baldacci.spark.SparkFactory
import org.apache.spark.sql.SparkSession

object HttpServiceWithSpark {

  val spark: SparkSession = SparkFactory.spark

  def getFileLines: Long = {
    val csvFile = "Users/lbaldacci/Develop/data/iclr2017_papers.csv"
    val fileData = spark.read.textFile(s"file:///$csvFile")
    fileData.count
  }
}
