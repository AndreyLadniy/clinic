package ecommerce.scheduling.view


import java.sql.Timestamp

import pl.newicom.dddd.aggregate.EntityId
import slick.driver.JdbcProfile
import slick.jdbc.meta.MTable._

import scala.concurrent.ExecutionContext

class CalendarTimeAllocationDao(implicit val profile: JdbcProfile, ec: ExecutionContext)  {
  import profile.api._

  val CalendarTimeAllocationTableName = "calendar_time_allocations"

  class CalendarTimeAllocations(tag: Tag) extends Table[CalendarTimeAllocationView](tag, CalendarTimeAllocationTableName) {
    def id = column[EntityId]("ID", O.PrimaryKey)
    def organizerId = column[EntityId]("ORGANIZER_ID")
    def start = column[Timestamp]("START")
    def end = column[Timestamp]("END")

    def * = (id, organizerId, start, end) <> (CalendarTimeAllocationView.tupled, CalendarTimeAllocationView.unapply)
  }

  val calendarTimeAllocations = TableQuery[CalendarTimeAllocations]

  /**
   * Queries impl
   */
  private val by_id = calendarTimeAllocations.findBy(_.id)
  private val by_organizer_id = calendarTimeAllocations.findBy(_.organizerId)


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

  def createOrUpdate(view: CalendarTimeAllocationView) = {
    calendarTimeAllocations.insertOrUpdate(view)
  }

  def updateInterval(id: EntityId, start: Timestamp, end: Timestamp) = {
    calendarTimeAllocations.filter(_.id === id).map(x => (x.start, x.end)).update((start, end))
  }
//  def updateStatus(viewId: EntityId, status: ReservationStatus.Value) = {
//    reservations.filter(_.id === viewId).map(_.status).update(status)
//  }

  def all =  calendarTimeAllocations.result

  def byId(id: EntityId) = by_id(id).result.headOption

  def byOrganizerId(organizerId: EntityId) = by_organizer_id(organizerId).result

  def remove(id: EntityId) = by_id(id).delete

  def ensureSchemaDropped =
    getTables(CalendarTimeAllocationTableName).headOption.flatMap {
      case Some(table) => calendarTimeAllocations.schema.drop.map(_ => ())
      case None => DBIO.successful(())
    }

  def ensureSchemaCreated =
    getTables(CalendarTimeAllocationTableName).headOption.flatMap {
      case Some(table) => DBIO.successful(())
      case None => calendarTimeAllocations.schema.create.map(_ => ())
    }

}
