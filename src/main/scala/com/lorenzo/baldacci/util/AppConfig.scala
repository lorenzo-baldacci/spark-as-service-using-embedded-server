package com.lorenzo.baldacci.util

import com.typesafe.config.{Config, ConfigFactory}

object AppConfig {

  private val conf: Config = ConfigFactory.load
  private val sparkMasterDef: String = conf.getString("spark.master")
  private val sparkAppNameDef: String = conf.getString("spark.appname")
  private val akkaHttpPortDef: Int = conf.getInt("akka.http.port")
  private val papersFolderDef: String = conf.getString("papers.folder")
  private val storageFolderDef: String = conf.getString("storage.folder")
  private val storageFileNameDef: String = conf.getString("storage.file.name")
  private val kaggleFileNameDef: String = conf.getString("kaggle.file")
  private val kaggleTemporaryFolderDef: String = conf.getString("kaggle.temp.folder")
  private val kaggleBinDef: String = conf.getString("kaggle.bin")
  private val kaggleDatasetDef: String = conf.getString("kaggle.dataset")



  var akkaHttpPort: Int = akkaHttpPortDef
  var sparkMaster: String = sparkMasterDef
  var sparkAppName: String = sparkAppNameDef
  var papersFolder: String = papersFolderDef
  var storageFolder: String = storageFolderDef
  var storageFileName: String = storageFileNameDef
  val kaggleFileName: String = kaggleFileNameDef
  val kaggleTemporaryFolder: String = kaggleTemporaryFolderDef
  val kaggleBin: String = kaggleBinDef
  val kaggleDataset: String = kaggleDatasetDef

  def main(args: Array[String]): Unit = {
    parse("-m localhost1 --akkaHttpPort 8080".split(" ").toList)
    print(sparkMaster, sparkAppName, akkaHttpPort)
  }

  val usage =
    s"""
This application exposes rest APIs of search engine based on Inverted Index. It uses Spark 2.3 as distributed engine and Akka HTTP for the rest API.
So, this application needs config params like AkkaWebPort to bind to, SparkMaster
and SparkAppName

Usage: spark-submit jarname.jar [options]
  Options:
  -h, --help
  -m, --master <master_url>                    spark://host:port, mesos://host:port, yarn, or local. Default: $sparkMasterDef
  -n, --name <name>                            A name of your application. Default: $sparkAppNameDef
  -p, --akkaHttpPort <portnumber>              Port where akka-http is binded. Default: $akkaHttpPortDef
  -f, --paperFolder <paper_folder>             Folder in which papers can be processed in batch. Default: $papersFolderDef
  -s, --storageFolder <storage_folder>         Folder in which the application will persist the inverted index. Default: $storageFolderDef
  -k, --kaggleBin <kaggle_bin>                 Kaggle binary which will be used to retrieve remotely the paper file. Default: $kaggleBinDef

Configured 6 routes:
1. homepage - http://host:port - says "hello world"
2. index papers from file - http://host:port/indexPapersFromFile - retrieves papers from file, process them in bulk, add to the index
3. index single paper - http://host:port/indexPaper/PAPER_ID|ABSTRACT - process the given paper and add it to the index
4. persist index - http://host:port/persistIndex - save the index in a permanent storage
5. retrieve persisted index - http://host:port/retrievePersistedIndex - substitutes the index with one stored previously
6. query the index - http://host:port/getPapers/WORD_TO_SEARCH - returns the list of papers where the word is found
7. refresh index from remote dataset - http://host:port/refreshIndexFromPublicDataset - replace the current index with remotely retrieved papers
  """

  def parse(list: List[String]): this.type = {

    list match {
      case Nil => this
      case ("--master" | "-m") :: value :: tail =>
        sparkMaster = value
        parse(tail)
      case ("--name" | "-n") :: value :: tail =>
        sparkAppName = value
        parse(tail)
      case ("--akkaHttpPort" | "-p") :: value :: tail =>
        akkaHttpPort = value.toInt
        parse(tail)
      case ("--paperFolder" | "-f") :: value :: tail =>
        papersFolder = value
        parse(tail)
      case ("--storageFolder" | "-s") :: value :: tail =>
        storageFolder = value
        parse(tail)
      case ("--kaggleBin" | "-k") :: value :: tail =>
        storageFolder = value
        parse(tail)
      case ("--help" | "-h") :: _ => printUsage(0)
      case _ => printUsage(1)
    }
  }

  def printUsage(exitNumber: Int): Nothing = {
    println(usage)
    sys.exit(status = exitNumber)
  }
}
