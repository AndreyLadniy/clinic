package ecommerce.gateway.api.web

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{HostConnectionPool, OutgoingConnection}
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.http.scaladsl.model.headers.{Connection, HttpOriginRange}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import ch.megard.akka.http.cors.{CorsDirectives, CorsSettings}
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

object HttpService {
  def props(interface: String, port: Int, askTimeout: FiniteDuration): Props =
    Props(new HttpService(interface, port)(askTimeout))
}

class HttpService(interface: String, port: Int)(implicit askTimeout: Timeout) extends Actor with ActorLogging
  with WebApiGatewayConfiguration with Directives {

  import context.dispatcher

  def materializerSettings: ActorMaterializerSettings = ActorMaterializerSettings(context.system)

  final implicit val materializer: ActorMaterializer = ActorMaterializer(Some(materializerSettings))

  Http(context.system).bindAndHandle(route, interface, port)

  log.info(s"Listening on $interface:$port")

  override def receive = Actor.emptyBehavior
  override def config: Config = context.system.settings.config

  val settings = CorsSettings.defaultSettings.copy(allowedOrigins = HttpOriginRange.*)

  val schedulingWriteFrontHost = "localhost"
  val schedulingWriteFrontPort = 9100

  val schedulingReadFrontHost = "localhost"
  val schedulingReadFrontPort = 9110

  def schedulingWriteFrontFlow = Http(context.system).outgoingConnection(schedulingWriteFrontHost, schedulingWriteFrontPort)

  val http = Http(context.system)

//  val schedulingReadFrontFlow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] = Http(context.system).outgoingConnection(schedulingReadFrontHost, schedulingReadFrontPort)
//
//  val schedulingReadFrontFlow1: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), HostConnectionPool] = Http(context.system).cachedHostConnectionPool[Int](schedulingReadFrontHost, schedulingReadFrontPort)

//  val schedulingWriteFrontFlow = Http().cachedHostConnectionPool(schedulingWriteFrontHost, schedulingWriteFrontPort)

  val addKeepAliveHeader = {
    val header = Connection.apply("keep-alive")

    request: HttpRequest => request.addHeader(header)
  }

  private def route: Route =
    path("health") {
      complete("")
    } ~
//    CorsDirectives.cors() {
      post {
        //      CorsDirectives.cors() {
        context =>
          Source.single(context.request).via(schedulingWriteFrontFlow).runWith(Sink.head).flatMap(context.complete(_))
        //      }
      } ~
      get {
//        CorsDirectives.cors() {
          context =>
//            Source.single(context.request -> 1).via(schedulingReadFrontFlow1).runWith(Sink.head).flatMap(p => context.complete(_))
            http.singleRequest(context.mapRequest(r => r.copy(uri = r.uri.withHost(schedulingReadFrontHost).withPort(9110))).request).flatMap(context.complete(_))

//            Source.single(context.request).map(addKeepAliveHeader).via(schedulingReadFrontFlow).runWith(Sink.head).flatMap(context.complete(_))

//        }
      }
//  ~
//      options {
//        complete("")
//      }
//    }

}

