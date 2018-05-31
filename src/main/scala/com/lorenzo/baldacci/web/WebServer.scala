package com.lorenzo.baldacci.web

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import com.lorenzo.baldacci.util.SparkFactory
import com.lorenzo.baldacci.web.HttpIntegration._

import scala.util.Try

/**
  * Http Server definition
  * Configured 4 routes:
  * 1. homepage - http://host:port - says "hello world"
  * 2. index papers from file - http://host:port/indexPapersFromFile - retrieves papers from file, process them in bulk, add to the index
  * 3. index single paper - http://host:port/indexPaper/PAPER_ID|ABSTRACT - process the given paper and add it to the index
  * 4. persist index - http://host:port/persistIndex - save the index in a permanent storage
  * 5. retrieve persisted index - http://host:port/retrievePersistedIndex - substitutes the index with one stored previously
  * 6. query the index - http://host:port/getPapers/WORD_TO_SEARCH - returns the list of papers where the word is found
  */
object WebServer extends HttpApp {
  override def postServerShutdown(attempt: Try[Done], system: ActorSystem): Unit = {
    SparkFactory.stop
  }

  override def routes: Route = {
    pathEndOrSingleSlash {
      get {
        complete(getHome)
      }
    } ~
      path("indexPapersFromFile") {
        post {
          complete(addPapersFromFile())
        }
      } ~
      path("indexPaper" / Segment) { segment =>
        post {
          complete(addToIndex(segment))
        }
      } ~
      path("persistIndex") {
        post {
          complete(storeIndex)
        }
      } ~
      path("retrievePersistedIndex") {
        post {
          complete(createOrReplaceIndexWithStorage)
        }
      } ~
      path("getPapers" / Segment) { segment =>
        get {
          complete(search(segment))
        }
      } ~
      path("test") {
        post {
          complete(test)
        }
      }
  }
}