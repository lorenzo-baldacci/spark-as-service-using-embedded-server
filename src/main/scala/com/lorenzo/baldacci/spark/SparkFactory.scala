package com.lorenzo.baldacci.spark

import com.lorenzo.baldacci.util.AppConfig
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

object SparkFactory {
  val spark: SparkSession = SparkSession.builder
    .master(AppConfig.sparkMaster)
    .appName(AppConfig.sparkAppName)
    .getOrCreate

  val sc: SparkContext = spark.sparkContext
  val sparkConf: SparkConf = sc.getConf
}
