package ecommerce.scheduling.view

import java.sql.Timestamp

import pl.newicom.dddd.aggregate.EntityId

import slick.dbio.Effect.{Read, Write}
import slick.driver.JdbcProfile
import slick.jdbc.meta.MTable.getTables
import slick.lifted.{PrimaryKey, ProvenShape}
import slick.profile.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.ExecutionContext

class AttendeeCalendarTimeAllocationDao(implicit val profile: JdbcProfile, ec: ExecutionContext)  {
  import profile.api._

  val AttendeesCalendarTimeAllocationsTableName = "calendar_time_allocation_by_attendee"

  class AttendeesCalendarTimeAllocations(tag: Tag) extends Table[AttendeeCalendarTimeAllocationView](tag, AttendeesCalendarTimeAllocationsTableName) {
    def calendarTimeAllocationId: Rep[EntityId] = column[EntityId]("CALENDAR_TIME_ALLOCATION_ID")
    def attendeeId: Rep[EntityId] = column[EntityId]("ATTENDEE_ID")
    def start: Rep[Timestamp] = column[Timestamp]("START")
    def end: Rep[Timestamp] = column[Timestamp]("END")

    def * : ProvenShape[AttendeeCalendarTimeAllocationView] = (calendarTimeAllocationId, attendeeId, start, end) <> (AttendeeCalendarTimeAllocationView.tupled, AttendeeCalendarTimeAllocationView.unapply)

    def pk: PrimaryKey = primaryKey("pk", (calendarTimeAllocationId, attendeeId))
  }

  val attendeesCalendarTimeAllocations = TableQuery[AttendeesCalendarTimeAllocations]

  /**
    * Queries impl
    */
//  private val by_calendarTimeAllocationId = attendeesCalendarTimeAllocations.findBy(_.calendarTimeAllocationId)
  private val by_attendee_id = attendeesCalendarTimeAllocations.findBy(_.attendeeId)


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

  def createOrUpdate(view: AttendeeCalendarTimeAllocationView): FixedSqlAction[Int, NoStream, Write] = {
    attendeesCalendarTimeAllocations.insertOrUpdate(view)
  }

  def updateInterval(calendarTimeAllocationId: EntityId, start: Timestamp, end: Timestamp): FixedSqlAction[Int, NoStream, Write] = {
    attendeesCalendarTimeAllocations.filter(_.calendarTimeAllocationId === calendarTimeAllocationId).map(x => (x.start, x.end)).update((start, end))
  }

  //  def updateStatus(viewId: EntityId, status: ReservationStatus.Value) = {
  //    attendeesCalendarTimeAllocations.filter(_.id === viewId).map(_.status).update(status)
  //  }

  def all: FixedSqlStreamingAction[Seq[AttendeeCalendarTimeAllocationView], AttendeeCalendarTimeAllocationView, Read] =  attendeesCalendarTimeAllocations.result

//  def byId(calendarTimeAllocationId: EntityId, attendeeId: EntityId) = by_id(id, attendeeId).result.headOption

  def byAttendeeId(attendeeId: EntityId): FixedSqlStreamingAction[Seq[AttendeeCalendarTimeAllocationView], AttendeeCalendarTimeAllocationView, Read] = by_attendee_id(attendeeId).result

//  def remove(id: EntityId, attendeeId: EntityId) = by_id(id).delete

  def ensureSchemaDropped =
    getTables(AttendeesCalendarTimeAllocationsTableName).headOption.flatMap {
      case Some(table) => attendeesCalendarTimeAllocations.schema.drop.map(_ => ())
      case None => DBIO.successful(())
    }

  def ensureSchemaCreated =
    getTables(AttendeesCalendarTimeAllocationsTableName).headOption.flatMap {
      case Some(table) => DBIO.successful(())
      case None => attendeesCalendarTimeAllocations.schema.create.map(_ => ())
    }

}
