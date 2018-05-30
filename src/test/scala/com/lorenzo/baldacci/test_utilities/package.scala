package com.lorenzo.baldacci

import java.io.{BufferedOutputStream, File}

import com.lorenzo.baldacci.fixtures.PrimitiveFixtures
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}

package object test_utilities {

  abstract class ScalaTestWithSparkSession extends FunSuite with BeforeAndAfterEach with Matchers with PrimitiveFixtures {

    @transient private var sparkSession: SparkSession = _

    def testWithSpark(name: String, testTags: org.scalatest.Tag*)(f: SparkSession => Unit): Unit = {
      test(name, testTags: _*) {
        f(sparkSession)
      }
    }

    def createTestTableFor(dataFrame: DataFrame, databaseName: String, tableName: String)(implicit sparkSession: SparkSession): DataFrame = {
      val TEMP_TABLE_NAME = someSmallString
      dataFrame.createOrReplaceTempView(TEMP_TABLE_NAME)
      sparkSession.sql(s"CREATE DATABASE IF NOT EXISTS $databaseName")
      sparkSession.sql(s"CREATE TABLE $databaseName.$tableName AS SELECT * FROM $TEMP_TABLE_NAME")
    }

    override protected def beforeEach(): Unit = {
      super.beforeEach()
      sparkSession = setupSparkSession()
    }

    override protected def afterEach(): Unit = {
      super.afterEach()
      stopSparkContext(sparkSession)
    }

    private def setupSparkSession(): SparkSession = {
      val sparkSession: SparkSession = SparkSession.builder
        .master("local")
        .appName("Test Applicastion")
        .getOrCreate
      import java.nio.file.Files
      val tempDir = Files.createTempDirectory("foobar").toFile
      generateHiveTestConfig(tempDir)
      sparkSession
    }

    private def stopSparkContext(sparkSession: SparkSession): Unit = {
      sparkSession.stop()
    }

    private def generateHiveTestConfig(tempDir: File): Unit = {
      import java.io.FileOutputStream
      val out = new FileOutputStream(new File(new File(this.getClass.getProtectionDomain.getCodeSource.getLocation
        .toURI), "hive-site.xml"))
      val elem = scala.xml.XML.loadString(
        s"""<configuration>
           |  <property>
           |    <name>hive.metastore.warehouse.dir</name>
           |    <value>${tempDir.getAbsolutePath}/hive</value>
           |    <description>local</description>
           |  </property>
           |  <property>
           |    <name>javax.jdo.option.ConnectionURL</name>
           |    <value>jdbc:derby:;databaseName=${tempDir.getAbsolutePath}/hive/metastore_db;create=true</value>
           |    <description>local</description>
           |  </property>
           |</configuration>""".stripMargin)
      val stream = new BufferedOutputStream(out)
      stream.write(elem.toString().getBytes)
      stream.flush()
      out.close()
    }
  }

}
