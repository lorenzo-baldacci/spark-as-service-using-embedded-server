package com.lorenzo.baldacci.search

import com.lorenzo.baldacci.search.test_utilities.ScalaTestWithSparkSession

class InvertedIndexProcessorTest extends ScalaTestWithSparkSession {
  testWithSpark("should return the inverted index") {
    implicit sparkSession =>
      import sparkSession.implicits._

      val key1 = someSmallString
      val key2 = someSmallString
      val key3 = someSmallString
      val key4 = someSmallString

      val rawData = Seq(
        Item(Option(key1), Option("some random : some text here (maybe)")),
        Item(Option(key2), Option("some other random text")),
        Item(Option(key3), Option("")),
        Item(Option(key4), None)
      )

      val rawItems = sparkSession.createDataset(rawData)

      val invertedIndexProcessor = new InvertedIndexProcessor
      val indexedText = invertedIndexProcessor.CreateInvertedIndex(rawItems).collect().toSet

      val expectedResult = Set(
        InverseIndex("some", key1),
        InverseIndex("random", key1),
        InverseIndex("text", key1),
        InverseIndex("here", key1),
        InverseIndex("maybe", key1),
        InverseIndex("some", key2),
        InverseIndex("other", key2),
        InverseIndex("random", key2),
        InverseIndex("text", key2)
      )

      indexedText shouldBe expectedResult
  }

  testWithSpark("should return empty set when nothing to index") {
    implicit sparkSession =>
      import sparkSession.implicits._

      val rawData = Seq.empty[Item]

      val rawItems = sparkSession.createDataset(rawData)

      val invertedIndexProcessor = new InvertedIndexProcessor
      val indexedText = invertedIndexProcessor.CreateInvertedIndex(rawItems).collect().toSet

      val expectedResult = Set.empty[InverseIndex]

      indexedText shouldBe expectedResult
  }
}
