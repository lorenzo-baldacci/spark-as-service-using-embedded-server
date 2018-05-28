package com.lorenzo.baldacci.search

import org.apache.spark.sql.{Dataset, SparkSession}

class InvertedIndexProcessor extends Serializable {

  val separators = """[^0-9a-z_]+"""

  def CreateInvertedIndex(itemsToIndex: Dataset[Item])(implicit sparkSession: SparkSession): Dataset[InverseIndex] = {
    import sparkSession.implicits._

    itemsToIndex
      .filter(i => i.text.isDefined && i.paperId.isDefined)
      .flatMap(paper =>
        paper.text.get.toLowerCase().split(separators)
          .map(_.trim)
          .filterNot(_ == "")
          .distinct
          .map(InverseIndex(_, paper.paperId.get))
      )
  }
}

case class Item(paperId: Option[String], text: Option[String])

case class InverseIndex(key: String, paperId: String)

