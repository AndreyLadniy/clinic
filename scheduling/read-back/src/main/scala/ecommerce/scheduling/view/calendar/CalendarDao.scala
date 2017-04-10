package ecommerce.scheduling.view.calendar

import java.time.OffsetDateTime

import ecommerce.scheduling.PostgresProfileWithDateTimeSupport
import ecommerce.scheduling.view.TimeAllocationManagerView
import pl.newicom.dddd.aggregate.EntityId
import slick.jdbc.SQLActionBuilder
import slick.jdbc.meta.MTable._
import slick.sql.SqlAction

import scala.concurrent.ExecutionContext

class CalendarDao(implicit val profile: PostgresProfileWithDateTimeSupport, ec: ExecutionContext)  {

  import profile.api._

  val CalendarsTableName = "calendars"

  class Calendars(tag: Tag) extends Table[CalendarView](tag, CalendarsTableName) {
    def calendarId = column[EntityId]("calendar_id", O.PrimaryKey)
    def summary = column[String]("summary")
    def description = column[Option[String]]("description")
    def location = column[Option[String]]("location")
    def timeZone = column[Option[String]]("time_zone")
    def deleted = column[Boolean]("deleted")

    def * = (calendarId, summary, description, location, timeZone, deleted) <> (CalendarView.tupled, CalendarView.unapply)
  }

  val calendars = TableQuery[Calendars]

  /**
   * Queries impl
   */
  private val by_id = calendars.findBy(_.calendarId)


  /**
   * Public interface
   */

/*
  def createIfNotExists(view: ReservationView)(implicit s: Session): ReservationView = {
    by_id(view.id).run.headOption.orElse {
      reservations.insert(view)
      Some(view)
    }.get
  }
*/

  def createOrUpdate(view: CalendarView) = {
    calendars.insertOrUpdate(view)
  }

  def updateSummary(calendarId: EntityId, summary: String) = {
    calendars.filter(_.calendarId === calendarId).map(_.summary).update(summary)
  }

  def updateDescription(calendarId: EntityId, description: Option[String]) = {
    calendars.filter(_.calendarId === calendarId).map(_.description).update(description)
  }

  def updateLocation(calendarId: EntityId, location: Option[String]) = {
    calendars.filter(_.calendarId === calendarId).map(_.location).update(location)
  }

  def updateTimeZone(calendarId: EntityId, timeZone: Option[String]) = {
    calendars.filter(_.calendarId === calendarId).map(_.timeZone).update(timeZone)
  }

  def updateDeleted(calendarId: EntityId, deleted: Boolean) = {
    calendars.filter(_.calendarId === calendarId).map(_.deleted).update(deleted)
  }

  def all =  calendars.result

  def byId(id: EntityId) = by_id(id).result.headOption


  def notifyEvent(event: String) = sql"""SELECT pg_notify('calendars', '#$event');""".as[Boolean]

  def ensureSchemaDropped =
    getTables(CalendarsTableName).headOption.flatMap {
      case Some(table) => calendars.schema.drop.map(_ => ())
      case None => DBIO.successful(())
    }

  def ensureSchemaCreated =
    getTables(CalendarsTableName).headOption.flatMap {
      case Some(table) => DBIO.successful(())
      case None => calendars.schema.create.map(_ => ())
    }

}
