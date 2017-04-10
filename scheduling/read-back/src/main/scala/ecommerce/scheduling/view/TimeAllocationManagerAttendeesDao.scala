package ecommerce.scheduling.view

import java.time.OffsetDateTime

import ecommerce.scheduling.PostgresProfileWithDateTimeSupport
import pl.newicom.dddd.aggregate.EntityId
import slick.jdbc.meta.MTable._
import slick.lifted.PrimaryKey

import scala.concurrent.ExecutionContext

class TimeAllocationManagerAttendeesDao(implicit val profile: PostgresProfileWithDateTimeSupport, ec: ExecutionContext)  {

  import profile.api._

  val TimeAllocationManagerViewTableName = "time_allocation_manager_attendees"


  class TimeAllocationManagers(tag: Tag) extends Table[TimeAllocationManagerAttendeesView](tag, TimeAllocationManagerViewTableName) {
    def timeAllocationManagerId: Rep[EntityId] = column[EntityId]("time_allocation_manager_id")
    def accepted: Rep[Boolean] = column[Boolean]("accepted", O.Default(false))
    def attendeeId: Rep[EntityId] = column[EntityId]("attendee_id")
    def organizerId: Rep[EntityId] = column[EntityId]("organizer_id")
    def start: Rep[OffsetDateTime] = column[OffsetDateTime]("start")
    def end: Rep[OffsetDateTime] = column[OffsetDateTime]("end")
    def responseStatus: Rep[String] = column[String]("response_status")

    def * = (timeAllocationManagerId, attendeeId, organizerId, accepted, start, end, responseStatus) <> (TimeAllocationManagerAttendeesView.tupled, TimeAllocationManagerAttendeesView.unapply)

    def pk: PrimaryKey = primaryKey("pk", (timeAllocationManagerId, attendeeId))
  }

  val calendarTimeAllocations = TableQuery[TimeAllocationManagers]

  /**
   * Queries impl
   */
  def calendarTimeAllocationsByPk(timeAllocationManagerId: Rep[EntityId], attendeeId: Rep[EntityId]) = {
    calendarTimeAllocations.filter(r => r.timeAllocationManagerId === timeAllocationManagerId && r.attendeeId === attendeeId)
  }

  private val byPkQuery = Compiled(calendarTimeAllocationsByPk _)
  private val byTimeAllocationManagerIdQuery = calendarTimeAllocations.findBy(_.timeAllocationManagerId)
  private val byAttendeeIdQuery = calendarTimeAllocations.findBy(_.attendeeId)


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

  def createOrUpdate(view: TimeAllocationManagerAttendeesView) = {
    calendarTimeAllocations.insertOrUpdate(view)
  }

  def updateAccepted(timeAllocationManagerId: EntityId, attendeeId: EntityId, accepted: Boolean) = {
    calendarTimeAllocations.filter(r => r.timeAllocationManagerId === timeAllocationManagerId && r.attendeeId === attendeeId).map(x=> (x.accepted, x.responseStatus)).update((accepted, "accepted"))
  }

  def updateDeclined(timeAllocationManagerId: EntityId, attendeeId: EntityId, accepted: Boolean) = {
    calendarTimeAllocations.filter(r => r.timeAllocationManagerId === timeAllocationManagerId && r.attendeeId === attendeeId).map(x=> (x.accepted, x.responseStatus)).update((accepted, "declined"))
  }

  def updateInterval(timeAllocationManagerId: EntityId, start: OffsetDateTime, end: OffsetDateTime) = {
    calendarTimeAllocations.filter(_.timeAllocationManagerId === timeAllocationManagerId).map(x => (x.start, x.end, x.accepted, x.responseStatus)).update((start, end, false, "needsAction"))
  }

//  def updateStatus(viewId: EntityId, status: ReservationStatus.Value) = {
//    reservations.filter(_.id === viewId).map(_.status).update(status)
//  }

  def all =  calendarTimeAllocations.result

  def byPk(timeAllocationManagerId: EntityId, attendeeId: EntityId) = byPkQuery(timeAllocationManagerId, attendeeId).result.headOption

  def byTimeAllocationManager(timeAllocationManagerId: EntityId) = byTimeAllocationManagerIdQuery(timeAllocationManagerId).result.headOption

  def byAttendeeId(attendeeId: EntityId) = byAttendeeIdQuery(attendeeId).result

  def remove(timeAllocationManagerId: EntityId, attendeeId: EntityId) = byPkQuery(timeAllocationManagerId, attendeeId).delete

  def ensureSchemaDropped =
    getTables(TimeAllocationManagerViewTableName).headOption.flatMap {
      case Some(table) => calendarTimeAllocations.schema.drop.map(_ => ())
      case None => DBIO.successful(())
    }

  def ensureSchemaCreated =
    getTables(TimeAllocationManagerViewTableName).headOption.flatMap {
      case Some(table) => DBIO.successful(())
      case None => calendarTimeAllocations.schema.create.map(_ => ())
    }

}
