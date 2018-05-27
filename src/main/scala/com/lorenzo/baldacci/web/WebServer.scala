package com.lorenzo.baldacci.web

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{HttpApp, Route}
import com.lorenzo.baldacci.spark.SparkFactory

/**
  * Http Server definition
  * Configured 4 routes:
  * 1. homepage - http://host:port - says "hello world"
  * 2. test - http://host:port/test - returns TEST SUCCESSFUL
   */
object WebServer extends HttpApp {
  override def routes: Route = {
    pathEndOrSingleSlash {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Hello World!! This is Akka responding</h1>" +
          s"<p>Spark version: ${SparkFactory.sc.version}</p>"))
      }
    } ~
      path("readLines") {
        get {
          complete("Number of lines in file: " + HttpServiceWithSpark.getFileLines)
        }
      }
}}