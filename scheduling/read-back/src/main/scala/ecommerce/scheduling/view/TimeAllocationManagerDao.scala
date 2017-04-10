package ecommerce.scheduling.view

import java.time.OffsetDateTime

import ecommerce.scheduling.PostgresProfileWithDateTimeSupport
import pl.newicom.dddd.aggregate.EntityId
import slick.jdbc.meta.MTable._

import scala.concurrent.ExecutionContext

class TimeAllocationManagerDao(implicit val profile: PostgresProfileWithDateTimeSupport, ec: ExecutionContext)  {

  import profile.api._

  val TimeAllocationManagersTableName = "time_allocation_managers"

  class TimeAllocationManagers(tag: Tag) extends Table[TimeAllocationManagerView](tag, TimeAllocationManagersTableName) {
    def timeAllocationManagerId = column[EntityId]("time_allocation_manager_id", O.PrimaryKey)
    def organizerId = column[EntityId]("organizer_id")
    def start = column[OffsetDateTime]("start")
    def end = column[OffsetDateTime]("end")
    def preparedForAccepting = column[Boolean]("prepared_for_accepting", O.Default(false))

    def * = (timeAllocationManagerId, organizerId, start, end, preparedForAccepting) <> (TimeAllocationManagerView.tupled, TimeAllocationManagerView.unapply)
  }

  val timeAllocationManagers = TableQuery[TimeAllocationManagers]

  /**
   * Queries impl
   */
  private val by_id = timeAllocationManagers.findBy(_.timeAllocationManagerId)
  private val by_organizer_id = timeAllocationManagers.findBy(_.organizerId)


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

  def createOrUpdate(view: TimeAllocationManagerView) = {
    timeAllocationManagers.insertOrUpdate(view)
  }

  def updateInterval(timeAllocationManagerId: EntityId, start: OffsetDateTime, end: OffsetDateTime) = {
    timeAllocationManagers.filter(_.timeAllocationManagerId === timeAllocationManagerId).map(x => (x.start, x.end, x.preparedForAccepting)).update((start, end, false))
  }

  def updatePreparedForAccepting(timeAllocationManagerId: EntityId, preparedForAccepting: Boolean) = {
    timeAllocationManagers.filter(_.timeAllocationManagerId === timeAllocationManagerId).map(x => x.preparedForAccepting).update(preparedForAccepting)
  }
  //  def updateStatus(viewId: EntityId, status: ReservationStatus.Value) = {
//    reservations.filter(_.id === viewId).map(_.status).update(status)
//  }

  def all =  timeAllocationManagers.result

  def byId(id: EntityId) = by_id(id).result.headOption

  def byOrganizerId(organizerId: EntityId) = by_organizer_id(organizerId).result

  def remove(id: EntityId) = by_id(id).delete

  def ensureSchemaDropped =
    getTables(TimeAllocationManagersTableName).headOption.flatMap {
      case Some(table) => timeAllocationManagers.schema.drop.map(_ => ())
      case None => DBIO.successful(())
    }

  def ensureSchemaCreated =
    getTables(TimeAllocationManagersTableName).headOption.flatMap {
      case Some(table) => DBIO.successful(())
      case None => timeAllocationManagers.schema.create.map(_ => ())
    }

}
