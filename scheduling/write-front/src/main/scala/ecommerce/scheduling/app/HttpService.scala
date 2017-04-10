package ecommerce.scheduling.app

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.util.Timeout
import ch.megard.akka.http.cors.CorsDirectives
import ecommerce.scheduling._
import org.json4s.Formats
import pl.newicom.dddd.delivery.protocol.Processed
import pl.newicom.dddd.messaging.command.CommandMessage
import pl.newicom.dddd.serialization.JsonSerHints.fromConfig
import pl.newicom.dddd.writefront.CommandDispatcher.UnknownCommandClassException
import pl.newicom.dddd.writefront.HttpCommandHandler

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

object HttpService {
  def props(interface: String, port: Int, askTimeout: FiniteDuration): Props =
    Props(new HttpService(interface, port)(askTimeout))
}

class HttpService(interface: String, port: Int)(implicit val timeout: Timeout)
  extends Actor with SchedulingFrontConfiguration with HttpCommandHandler with ActorLogging {

  implicit val formats: Formats = fromConfig(config)

  Http(context.system).bindAndHandle(route, interface, port)

  log.info(s"Listening on $interface:$port")

  override def receive: Receive = Actor.emptyBehavior

  override def offices = Set(CalendarOfficeId, TimeAllocationManagerOfficeId, CalendarTimeManagerOfficeId)

  import context.dispatcher

  private def route = /*logRequestResult("sales")*/ {
    pathPrefix("ecommerce") {
      path("scheduling" / "allocation") {
//        CorsDirectives.cors() {
//          time("Handle command request", handle[ecommerce.scheduling.TimeAllocationManagerCommand])
          handle[TimeAllocationManagerCommand]
//        }
      } ~
      path("calendar") {
//        CorsDirectives.cors() {
//          time("Handle command request", handle[ecommerce.scheduling.CalendarCommand])
          handle[CalendarCommand]
//        }
      } ~
      path("scheduling" / "time") {
//        CorsDirectives.cors() {
          handle[CalendarTimeManagerCommand]
//        }
      }
    }
  }

  override def dispatch(msg: CommandMessage): Future[OfficeResponse] = {
    officeRepresentative(msg).map {
      forward(msg)
    }.getOrElse {
      Future.failed(UnknownCommandClassException(msg.command))
    }
  }

  private def officeRepresentative(msg: CommandMessage): Option[ActorRef] =
    offices.find(
      _.messageClass.isAssignableFrom(msg.command.getClass)
    ).map(
      officeActor
    )


  private def forward(msg: CommandMessage)(officeRepresentative: ActorRef): Future[OfficeResponse] = {
    import akka.pattern.ask


    time("Send command", officeRepresentative ? msg)
      .mapTo[Processed]
      .map(_.result)
  }

  def time[R](description: => String, block: => R)(implicit ec: ExecutionContext) : R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()

    log.debug(s"[$description] Elapsed time: ${t1- t0} ns")

    result
  }

}