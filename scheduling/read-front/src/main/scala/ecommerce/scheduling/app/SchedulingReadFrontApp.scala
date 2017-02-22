package ecommerce.scheduling.app

import akka.actor.Props
import akka.kernel.Bootable
import akka.stream.actor.ActorPublisher
import com.github.mauricio.async.db.Configuration
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser
import com.typesafe.config.Config
import ecommerce.scheduling.{HttpService, SchedulingReadFrontConfiguration}

import scala.concurrent.Await

class SchedulingReadFrontApp extends Bootable {

  override def systemName = "scheduling-read-front"

  def startup() = {
     new SchedulingReadFrontConfiguration {
       override def config: Config = SchedulingReadFrontApp.this.config

       import httpService._
       system.actorOf(HttpService.props(interface, port, askTimeout), "http-service")
     }
   }

}

object SchedulingReadFrontApp {

  def main(args: Array[String]): Unit = {
    val app = new SchedulingReadFrontApp

    app.startup()

    sys.addShutdownHook(app.shutdown())
  }

}

import scala.concurrent.duration._

object PostgresListener {
  def props(channel: String) = Props(new PostgresListener(channel))
}

class PostgresListener(channel: String) extends ActorPublisher[String] {

  override def receive = {
    case _ ⇒
//      val configuration = URLParser.parse(s"jdbc://postgresql://$host:$port/$db?user=$user&password=$password")
      val connection = new PostgreSQLConnection(Configuration(username = "postgres", password = Some("63S7kv8gLLtiRp2b"), database = Some("clinic")))


      Await.result(connection.connect, 5.seconds)

      connection.sendQuery(s"LISTEN $channel")
      connection.registerNotifyListener { message ⇒ onNext(message.payload) }
  }
}