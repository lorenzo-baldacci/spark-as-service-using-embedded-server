package com.lorenzo.baldacci.search

import org.apache.spark.sql.{Dataset, SparkSession}

class InvertedIndexProcessor extends Serializable {

  val separators = Array(' ', ',', ';', '.', '(', ')', '-', '{', '}', '[', ']', ':')

  def CreateInvertedIndex(itemsToIndex: Dataset[Item])(implicit sparkSession: SparkSession): Dataset[InverseIndex] = {
    import sparkSession.implicits._
    itemsToIndex.flatMap(paper => {
      paper.text.split(separators)
        .map(_.trim)
        .filterNot(_ == "")
        .distinct
        .map(InverseIndex(_, paper.paperId))
    })
  }
}

case class Item(paperId: String, text: String)
case class InverseIndex(key: String, paperId: String)

