package com.spoddutur.web

import com.spoddutur.spark.SparkFactory
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

/**
  * Created by sruthi on 03/07/17.
  * Service class computing the value for route bindings "/activeStreams" and "/count" respectively.
  */
object HttpService {

  val sc: SparkContext = SparkFactory.sc
  val spark: SparkSession = SparkFactory.spark

  // To server http://host:port/count route binding
  // Random spark job counting a seq of integers split into 25 partitions
  def count(): String = sc.parallelize(0 to 500000, 25).count.toString

  // To server http://host:port/activeStreams route binding
  // Returns how many streams are active in sparkSession currently
  def activeStreamsInSparkContext(): Int = SparkFactory.spark.streams.active.length

  def getFileLines: Long = {
    val csvFile = "Users/lbaldacci/Develop/data/iclr2017_papers.csv"
    val fileData = spark.read.textFile(s"file:///$csvFile")
    fileData.count
  }
}
