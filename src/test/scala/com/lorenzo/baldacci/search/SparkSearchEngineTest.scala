package com.lorenzo.baldacci.search

import com.lorenzo.baldacci.search.test_utilities.ScalaTestWithSparkSession

class SparkSearchEngineTest extends ScalaTestWithSparkSession {
  testWithSpark("some name"){
    sparkSession =>

    SparkSearchEngine.createOrReplaceIndex

    2 shouldBe 2
  }
}
