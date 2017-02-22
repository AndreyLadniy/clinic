package ecommerce.scheduling

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.Config
import ecommerce.scheduling.app.{CalendarTimeAllocationViewEndpoint, PostgresListener}
import org.json4s.Formats
import pl.newicom.dddd.serialization.JsonSerHints._
import pl.newicom.dddd.streams.ImplicitMaterializer
import slick.driver.PostgresDriver

import scala.concurrent.duration.FiniteDuration

object HttpService {
  def props(interface: String, port: Int, askTimeout: FiniteDuration): Props =
    Props(new HttpService(interface, port)(askTimeout))
}

class HttpService(interface: String, port: Int)(implicit askTimeout: Timeout) extends Actor with ActorLogging
  with SchedulingReadFrontConfiguration with ImplicitMaterializer with Directives {

  import context.dispatcher

  implicit val formats: Formats = fromConfig(config)
  implicit val profile = PostgresDriver

  Http(context.system).bindAndHandle(route, interface, port)

  log.info(s"Listening on $interface:$port")

  override def receive = Actor.emptyBehavior
  override def config: Config = context.system.settings.config

  lazy val endpoints: CalendarTimeAllocationViewEndpoint = CalendarTimeAllocationViewEndpoint()

//  val events = stream

  val pr = stream.runForeach(println(_))

  def channelNotification: Flow[Message, Message, Any] = {

    val incommingMessages = Sink.ignore

    val outgoingMessage: Source[Message, NotUsed] = stream.map(TextMessage(_)).keepAlive(FiniteDuration(1, TimeUnit.SECONDS), () => TextMessage.Strict("Heart Beat"))

    Flow.fromSinkAndSource(incommingMessages, outgoingMessage)

//    Flow[Message].mapConcat {
//      case tm: TextMessage =>
//        TextMessage("pg_event") :: Nil
//      case bm: BinaryMessage =>
//        // ignore binary messages but drain content to avoid the stream being clogged
//        bm.dataStream.runWith(Sink.ignore)
//        Nil
//    }
  }

  private def route = (provide( viewStore ) & pathPrefix("ecommerce" / "scheduling"))(endpoints) ~
    path("channel") {
      handleWebSocketMessages(channelNotification)
    }

  def stream: Source[String, NotUsed] = {
    val dataPublisherRef = context.actorOf(PostgresListener.props("events"))
    val dataPublisher = ActorPublisher[String](dataPublisherRef)

//    dataPublisherRef ! "go"

    Source.fromPublisher(dataPublisher)
//      .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.right).run()
//      .map(ServerSentEvent(_))
//      .via(WithHeartbeats(10.second))
  }

}

