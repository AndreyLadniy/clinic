package ecommerce.scheduling.app

import akka.actor.{ActorLogging, ActorRef, Props, Terminated}
import akka.event.LookupClassification
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

  case object Subscribe

  case object Unsubscribe

  case class SendMessage(message: String)

  def props(channel: String) = Props(new PostgresListener(channel))
}

class PostgresListener(channel: String) extends ActorPublisher[String] with ActorLogging {

  var connection: PostgreSQLConnection = null

  var listeners = List.empty[ActorRef]

  override def preStart(): Unit = {
    connection = new PostgreSQLConnection(Configuration(username = "postgres", password = Some("63S7kv8gLLtiRp2b"), database = Some("clinic")))

    Await.result(connection.connect, 5.seconds)

    connection.sendQuery(s"LISTEN $channel")
    connection.registerNotifyListener { message ⇒ self ! PostgresListener.SendMessage(message.payload) }
  }

  override def postStop(): Unit = {
    connection.clearNotifyListeners()
  }

  override def receive = {

    case PostgresListener.Subscribe =>
      listeners = sender() :: listeners

      context.watch(sender())

      log.debug("Listener subsribed")
    case PostgresListener.Unsubscribe =>
      listeners = listeners.filterNot(_ == sender())

      log.debug("Listener unsubsribed")
    case Terminated(actor) =>
      listeners = listeners.filterNot(_ == actor)

      log.debug("Listener unsubsribed")
    case PostgresListener.SendMessage(message) =>
      onNext(message)
    case _ ⇒
//      val configuration = URLParser.parse(s"jdbc://postgresql://$host:$port/$db?user=$user&password=$password")


  }
}