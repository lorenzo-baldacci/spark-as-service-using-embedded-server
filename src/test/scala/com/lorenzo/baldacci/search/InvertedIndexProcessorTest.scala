package com.lorenzo.baldacci.search

import com.lorenzo.baldacci.test_utilities.ScalaTestWithSparkSession

class InvertedIndexProcessorTest extends ScalaTestWithSparkSession {
  testWithSpark("should process summaries in batch") {
    implicit sparkSession =>
      import sparkSession.implicits._

      val key1 = someSmallString
      val key2 = someSmallString
      val key3 = someSmallString
      val key4 = someSmallString

      val rawData = Seq(
        Summary(Option(key1), Option("some random : some text here (maybe)")),
        Summary(Option(key2), Option("some other random text")),
        Summary(Option(key3), Option("")),
        Summary(Option(key4), None),
        Summary(None, Option("this one will be lost"))
      )

      val rawItems = sparkSession.createDataset(rawData)

      val invertedIndexProcessor = new InvertedIndexProcessor
      val indexedText = invertedIndexProcessor.CreateInvertedIndex(rawItems).collect().toSet

      val expectedIndexedText = Set(
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

      indexedText shouldBe expectedIndexedText
  }

  testWithSpark("should return empty set when the batch is empty") {
    implicit sparkSession =>
      import sparkSession.implicits._

      val rawData = Seq.empty[Summary]

      val rawItems = sparkSession.createDataset(rawData)

      val invertedIndexProcessor = new InvertedIndexProcessor
      val indexedText = invertedIndexProcessor.CreateInvertedIndex(rawItems).collect().toSet

      val expectedResult = Set.empty[InverseIndex]

      indexedText shouldBe expectedResult
  }

  testWithSpark("should process a single paper") {
    implicit sparkSession =>
      val key = someSmallString
      val paper = Summary(Option(key), Option("some random : some text here (maybe)"))

      val invertedIndexProcessor = new InvertedIndexProcessor
      val indexedText = invertedIndexProcessor.CreateInvertedIndex(paper).collect().toSet

      val expectedIndexedText = Set(
        InverseIndex("some", key),
        InverseIndex("random", key),
        InverseIndex("text", key),
        InverseIndex("here", key),
        InverseIndex("maybe", key)
      )

      indexedText shouldBe expectedIndexedText
  }

  testWithSpark("should return an empty set when papers are not completely defined") {
    implicit sparkSession =>
      val key1 = someSmallString
      val paper1 = Summary(Option(key1), None)
      val paper2 = Summary(None, Option("some random : some text here (maybe)"))

      val invertedIndexProcessor = new InvertedIndexProcessor
      val indexedText1 = invertedIndexProcessor.CreateInvertedIndex(paper1).collect().toSet
      val indexedText2 = invertedIndexProcessor.CreateInvertedIndex(paper2).collect().toSet

      val expectedIndexedText = Set.empty[InverseIndex]

      indexedText1 shouldBe expectedIndexedText
      indexedText2 shouldBe expectedIndexedText
  }
}
