package ecommerce.scheduling

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, RunnableGraph, Sink, Source}
import akka.util.Timeout
import ch.megard.akka.http.cors.{CorsDirectives, CorsSettings}
import ch.megard.akka.http.cors.HttpHeaderRange.*
import com.typesafe.config.Config
import ecommerce.scheduling.app.{CalendarTimeAllocationViewEndpoint, PostgresListener}
import ecommerce.scheduling.view.CalendarTimeAllocationProjection
import org.json4s.Formats
import pl.newicom.dddd.serialization.JsonSerHints._
import pl.newicom.dddd.streams.ImplicitMaterializer
import pl.newicom.dddd.view.sql.SqlViewStore

import scala.concurrent.duration.FiniteDuration

object HttpService {
  def props(interface: String, port: Int, askTimeout: FiniteDuration): Props =
    Props(new HttpService(interface, port)(askTimeout))
}

class HttpService(interface: String, port: Int)(implicit askTimeout: Timeout) extends Actor with ActorLogging
  with SchedulingReadFrontConfiguration with ImplicitMaterializer with Directives {

  import context.dispatcher

  implicit val formats: Formats = fromConfig(config)
  implicit val profile = PostgresProfileWithDateTimeSupport

  Http(context.system).bindAndHandle(route, interface, port)

  log.info(s"Listening on $interface:$port")

  override def receive = Actor.emptyBehavior
  override def config: Config = context.system.settings.config

  lazy val endpoints: CalendarTimeAllocationViewEndpoint = CalendarTimeAllocationViewEndpoint()


  val settings = CorsSettings.defaultSettings.copy(allowedOrigins = HttpOriginRange.*)

  val postgresEventsSource: Source[String, NotUsed] =
    Source.fromPublisher(
      ActorPublisher[String](
        context.actorOf(PostgresListener.props(CalendarTimeAllocationProjection.notifyChannel))
      )
    )

  val webSocketOutgoingMessagesSource: Source[TextMessage, NotUsed] =
    postgresEventsSource
      .map(TextMessage(_))
      .keepAlive(FiniteDuration(10, TimeUnit.SECONDS), () => TextMessage.Strict("Heart Beat"))
      .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.right)
      .run()

  private def route: Route = {
    (provide( new SqlViewStore(config) ) & pathPrefix("ecommerce" / "scheduling"))(endpoints) ~
      path("channel") {
        handleWebSocketMessages(Flow.fromSinkAndSource(Sink.ignore, webSocketOutgoingMessagesSource))
      }
  }



}

