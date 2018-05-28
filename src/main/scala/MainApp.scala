import akka.http.scaladsl.settings.ServerSettings
import com.lorenzo.baldacci.util.AppConfig
import com.lorenzo.baldacci.web.WebServer
import com.typesafe.config.ConfigFactory

object MainApp extends App {

  AppConfig.parse(this.args.toList)


  WebServer.startServer("localhost", AppConfig.akkaHttpPort, ServerSettings(ConfigFactory.load))

  println(s"Server online at http://localhost:", AppConfig.akkaHttpPort, "/")
}
