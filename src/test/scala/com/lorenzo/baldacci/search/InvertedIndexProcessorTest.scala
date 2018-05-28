package com.lorenzo.baldacci.search

import com.lorenzo.baldacci.search.test_utilities.ScalaTestWithSparkSession

class InvertedIndexProcessorTest extends ScalaTestWithSparkSession {
  testWithSpark("should return the indexed text") {
    implicit sparkSession =>
      import sparkSession.implicits._

      val rawData = Seq(Item("key1", "some random : some text here (maybe)"), Item("key2", "some other random text"))

      val rawItems = sparkSession.createDataset(rawData)

      val invertedIndexProcessor = new InvertedIndexProcessor
      val indexedText = invertedIndexProcessor.CreateInvertedIndex(rawItems).collect().toSet

      val expectedResult = Set(
        InverseIndex("some", "key1"),
        InverseIndex("random", "key1"),
        InverseIndex("text", "key1"),
        InverseIndex("here", "key1"),
        InverseIndex("maybe", "key1"),
        InverseIndex("some", "key2"),
        InverseIndex("other", "key2"),
        InverseIndex("random", "key2"),
        InverseIndex("text", "key2")
      )

      indexedText shouldBe expectedResult
  }
}
