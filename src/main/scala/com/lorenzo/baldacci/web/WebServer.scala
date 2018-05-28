package com.lorenzo.baldacci.web

import akka.http.scaladsl.server.{HttpApp, Route}
import com.lorenzo.baldacci.web.HttpService._

/**
  * Http Server definition
  * Configured 4 routes:
  * 1. homepage - http://host:port - says "hello world"
  * 2. test - http://host:port/test - returns TEST SUCCESSFUL
  */
object WebServer extends HttpApp {

  override def routes: Route = {
    pathEndOrSingleSlash {
      get {complete(getHome)}
    } ~
    path("readLines") {
      get {complete(getFileLines)}
    } ~
    path("createOrReplaceIndex") {
      put {complete(createOrReplaceIndex)}
    } ~
    path("getPapers" / Segment) { segment =>
      get {complete(getPapers(segment))}
    }
  }
}