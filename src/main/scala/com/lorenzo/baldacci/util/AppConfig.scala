package com.lorenzo.baldacci.util

import com.typesafe.config.{Config, ConfigFactory}

object AppConfig {

  private val conf: Config = ConfigFactory.load
  private val sparkMasterDef: String = conf.getString("spark.master")
  private val sparkAppNameDef: String = conf.getString("spark.appname")
  private val akkaHttpPortDef: Int = conf.getInt("akka.http.port")
  private val appFolderPapersDef: String = conf.getString("app.papers.folder")
  private val appFolderStorageDef: String = conf.getString("app.storage.folder")
  private val appStorageFileNameDef: String = conf.getString("app.storage.name")

  var akkaHttpPort: Int = akkaHttpPortDef
  var sparkMaster: String = sparkMasterDef
  var sparkAppName: String = sparkAppNameDef
  var appPapersFolder: String = appFolderPapersDef
  var appStorageFolder: String = appFolderStorageDef
  var appStorageFileName: String = appStorageFileNameDef

  def main(args: Array[String]): Unit = {
    parse("-m localhost1 --akkaHttpPort 8080".split(" ").toList)
    print(sparkMaster, sparkAppName, akkaHttpPort)
  }

  //TODO: will edit this at the end of the development
  val usage =
    s"""
This application comes as Spark2.1-REST-Service-Provider using an embedded,
Reactive-Streams-based, fully asynchronous HTTP server (i.e., using akka-http).
So, this application needs config params like AkkaWebPort to bind to, SparkMaster
and SparkAppName

Usage: spark-submit spark-as-service-using-embedded-server.jar [options]
  Options:
  -h, --help
  -m, --master <master_url>                    spark://host:port, mesos://host:port, yarn, or local. Default: $sparkMasterDef
  -n, --name <name>                            A name of your application. Default: $sparkAppNameDef
  -p, --akkaHttpPort <portnumber>              Port where akka-http is binded. Default: $akkaHttpPortDef

Configured 4 routes:
1. homepage - http://host:port - says "hello world"
2. version - http://host:port/version - tells "spark version"
3. activeStreams - http://host:port/activeStreams - tells how many spark streams are active currently
4. count - http://host:port/count - random spark job to count a seq of integers
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
      case ("--help" | "-h") :: _ => printUsage(0)
      case _ => printUsage(1)
    }
  }

  def printUsage(exitNumber: Int): Nothing = {
    println(usage)
    sys.exit(status = exitNumber)
  }
}
