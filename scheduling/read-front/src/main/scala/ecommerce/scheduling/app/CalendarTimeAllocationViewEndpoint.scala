package ecommerce.scheduling.app

import akka.NotUsed
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import ecommerce.scheduling.ReadEndpoint
import ecommerce.scheduling.view.CalendarTimeAllocationDao
import org.json4s.Formats
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

case class CalendarTimeAllocationViewEndpoint(implicit ec: ExecutionContext, profile: JdbcProfile, formats: Formats) extends ReadEndpoint {

  lazy val dao = new CalendarTimeAllocationDao()

  def route(viewStore: Database): Route = {

    path("allocation" / "all") {
      get {
        complete {
          viewStore.run {
            dao.all
          }
        }
      }
    } ~
    path("allocation" / Segment) { id =>
      get {
        onSuccess(viewStore.run(dao.byId(id))) {
          case Some(res) => complete(res)
          case None => complete(StatusCodes.NotFound -> "unknown calendar time allocation")
        }
      }
    }

  }

}
