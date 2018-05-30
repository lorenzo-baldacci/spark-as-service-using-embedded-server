package com.lorenzo.baldacci.search

import org.apache.spark.sql.{Dataset, SparkSession}

class InvertedIndexProcessor extends Serializable {

  val separators = "[^0-9a-z_]+"

  def CreateInvertedIndex(itemsToIndex: Dataset[Summary])(implicit sparkSession: SparkSession): Dataset[InverseIndex] = {
    import sparkSession.implicits._

    itemsToIndex
      .filter(i => i.text.isDefined && i.paperId.isDefined)
      .flatMap(toInverseIndex)
  }

  def CreateInvertedIndex(itemToIndex: Summary)(implicit sparkSession: SparkSession): Dataset[InverseIndex] = {
    import sparkSession.implicits._

    val inverseIndex = itemToIndex match {
      case Summary(paperId, text) if paperId.isDefined && text.isDefined => toInverseIndex(itemToIndex)
      case _ => Array.empty[InverseIndex]
    }

    sparkSession.createDataset(inverseIndex)
  }

  private def toInverseIndex(item: Summary): Array[InverseIndex] = {
    item.text.get.toLowerCase().split(separators)
      .map(_.trim)
      .filterNot(_ == "")
      .distinct
      .map(InverseIndex(_, item.paperId.get))
  }
}

