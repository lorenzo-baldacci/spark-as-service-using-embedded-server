package com.lorenzo.baldacci.search

import com.lorenzo.baldacci.test_utilities.ScalaTestWithSparkSession
import com.lorenzo.baldacci.util.AppConfig
import com.lorenzo.baldacci.util.IOLayer.readFilesFromFolder
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions._

class SparkSearchEngineTest extends ScalaTestWithSparkSession {
  testWithSpark("index should be empty at start") {
    implicit sparkSession =>
      val sparkSearchEngine = new SparkSearchEngine
      sparkSearchEngine.getIndex.collect() shouldBe Seq.empty[InverseIndex]
  }

  testWithSpark("should add papers in bulk to index") {
    implicit sparkSession =>
      import sparkSession.implicits._
      val sparkSearchEngine = new SparkSearchEngine
      val invertedIndexProcessor = new InvertedIndexProcessor

      val summaries = someSmallSequenceOf(somePaper)
      val summariesDS = sparkSession.createDataset(summaries)
      sparkSearchEngine.addToIndex(summariesDS)

      val actualIndex = sparkSearchEngine.getIndex.collect().toSet
      val expectedIndex = invertedIndexProcessor.CreateInvertedIndex(summariesDS).collect().toSet

      actualIndex shouldBe expectedIndex
  }

  testWithSpark("indexed single papers should be added to the existing index") {
    implicit sparkSession =>
      import sparkSession.implicits._
      val sparkSearchEngine = new SparkSearchEngine
      val invertedIndexProcessor = new InvertedIndexProcessor

      val summaries = someSmallSequenceOf(somePaper)
      val summariesDS = sparkSession.createDataset(summaries)
      sparkSearchEngine.addToIndex(summariesDS)

      val singlePaperSummary = Summary(Option(someSmallString), Option(someSmallText))
      sparkSearchEngine.addToIndex(singlePaperSummary)

      val actualIndex = sparkSearchEngine.getIndex.collect().toSet
      val expectedIndex = invertedIndexProcessor.CreateInvertedIndex(summariesDS).collect().toSet ++
        invertedIndexProcessor.CreateInvertedIndex(singlePaperSummary).collect().toSet

      actualIndex shouldBe expectedIndex
  }

  testWithSpark("should retrieve paper ids") {
    implicit sparkSession =>
      val sparkSearchEngine = new SparkSearchEngine

      val wordToSearch = someSmallString
      val singlePaperSummary = Summary(Option(someSmallString), Option(s"$someSmallText $wordToSearch"))
      sparkSearchEngine.addToIndex(singlePaperSummary)

      val searchResult = sparkSearchEngine.getPaperIds(wordToSearch)

      searchResult should contain(singlePaperSummary.paperId.get)
  }

  testWithSpark("should retrieve nothing when searched word is not part of the index") {
    implicit sparkSession =>
      import sparkSession.implicits._
      val sparkSearchEngine = new SparkSearchEngine

      val summaries = someSmallSequenceOf(somePaper)
      val summariesDS = sparkSession.createDataset(summaries)
      sparkSearchEngine.addToIndex(summariesDS)

      val wordToSearch = someSmallString + someSmallString
      val searchResult = sparkSearchEngine.getPaperIds(wordToSearch)

      searchResult shouldBe Array.empty[String]
  }
}
