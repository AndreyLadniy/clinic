package ecommerce.scheduling.app

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.CorsDirectives
import ecommerce.scheduling.view.calendar.CalendarDao
import ecommerce.scheduling.view.{TimeAllocationManagerAttendeesDao, TimeAllocationManagerDao}
import ecommerce.scheduling.{PostgresProfileWithDateTimeSupport, ReadEndpoint}
import org.json4s.Formats
import pl.newicom.dddd.view.sql.SqlViewStore

import scala.concurrent.ExecutionContext

case class CalendarTimeAllocationViewEndpoint(implicit ec: ExecutionContext, profile: PostgresProfileWithDateTimeSupport, formats: Formats) extends ReadEndpoint {

  lazy val dao = new TimeAllocationManagerDao()

  lazy val dao2 = new TimeAllocationManagerAttendeesDao()

  lazy val calendarDao = new CalendarDao()

  def route(viewStore: SqlViewStore): Route = {

    path("calendars" / Segment) { calendarId =>
      get {
//        CorsDirectives.cors() {
          complete {
            viewStore.run {
              calendarDao.byId(calendarId)
            }
          }
//        }
      }
    } ~
    path("calendars") {
      get {
//        CorsDirectives.cors() {
          complete {
            viewStore.run {
              calendarDao.all
            }
//          }
        }
      }
    } ~
    path(Segment / "allocations" / "all") { organizerId =>
      get {
//        CorsDirectives.cors() {
          complete {
            viewStore.run {
              dao.byOrganizerId(organizerId)
            }
          }
//        }
      }
    } ~
    path(Segment / "allocations" / "attendees") { organizerId =>
      get {
//        CorsDirectives.cors() {
          complete {
            viewStore.run {
              dao2.all
            }
          }
//        }
      }
    } ~
    path(Segment / "allocations" / Segment) {case (organizerId, timeAllocationManagerId) =>
      get {
        onSuccess(viewStore.run(dao.byId(timeAllocationManagerId))) {
          case Some(res) => complete(res)
          case None => complete(StatusCodes.NotFound -> "unknown calendar time allocation")
        }
      }
    }


  }

}
