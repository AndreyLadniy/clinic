package ecommerce.scheduling.view

import ecommerce.scheduling._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}
import org.json4s.{Extraction, Formats}
import pl.newicom.dddd.messaging.event.OfficeEventMessage
import pl.newicom.dddd.view.sql.Projection
import pl.newicom.dddd.view.sql.Projection.ProjectionAction
import slick.dbio.Effect.Write
import slick.dbio.{DBIOAction, Effect}
import slick.sql.SqlStreamingAction

import scala.concurrent.ExecutionContext

class CalendarTimeAllocationProjection(timeAllocationManagerDao: TimeAllocationManagerDao, timeAllocationManagerAttendeesDao: TimeAllocationManagerAttendeesDao, notificationSupport: NotificationSupport)(implicit ec: ExecutionContext, formats: Formats) extends Projection {

  def notifyEvent(eventMessage: OfficeEventMessage, eventType: String): SqlStreamingAction[Vector[Boolean], Boolean, Effect] =
    notificationSupport.notifyEvent(
      CalendarTimeAllocationProjection.notifyChannel,
      compact(
        render(
          ("event" -> eventType) ~ ("payload" -> Extraction.decompose(eventMessage.event)) ~ ("sequenceNr" -> eventMessage.eventNumber)
        )
      )
    )

  override def consume(eventMessage: OfficeEventMessage): ProjectionAction[Write] = {
    eventMessage.event match {

      case TimeAllocationManagerCreated(timeAllocationManagerId, organizerId, start, end) =>
        val createOrUpdate = timeAllocationManagerDao.createOrUpdate(
          TimeAllocationManagerView(timeAllocationManagerId, organizerId, start, end, preparedForAccepting = false)
        )

        createOrUpdate >> notifyEvent(eventMessage, "TimeAllocationManagerCreated")


      case AttendeeTimeAllocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) =>
        val updateTimeAllocationManagerPreparedForAccepting = timeAllocationManagerDao.updatePreparedForAccepting(
          timeAllocationManagerId,
          preparedForAccepting = false
        )

        val prepareAttendeeTimeForAllocation = timeAllocationManagerAttendeesDao.createOrUpdate(
          TimeAllocationManagerAttendeesView(timeAllocationManagerId, attendeeId, organizerId, accepted = false, start, end, "needsAction")
        )

        updateTimeAllocationManagerPreparedForAccepting >> prepareAttendeeTimeForAllocation >> notifyEvent(eventMessage, "AttendeeTimeAllocationRequested")

      case TimeAllocationManagerIntervalMoved(timeAllocationManagerId, organizerId, attendees, start, end) =>
        val updateTimeAllocationManagerInterval = timeAllocationManagerDao.updateInterval(
          timeAllocationManagerId,
          start,
          end
        )

        val updateTimeAllocationManagerAttendeesInterval = timeAllocationManagerAttendeesDao.updateInterval(
          timeAllocationManagerId,
          start,
          end
        )

        updateTimeAllocationManagerInterval >> updateTimeAllocationManagerAttendeesInterval >> notifyEvent(eventMessage, "TimeAllocationManagerIntervalMoved")

      case AttendeeTimeAllocationAccepted(timeAllocationManagerId, attendeeId) =>
        val updateAccepted = timeAllocationManagerAttendeesDao.updateAccepted(timeAllocationManagerId, attendeeId, accepted = true)

        updateAccepted >> notifyEvent(eventMessage, "AttendeeTimeAllocationAccepted")

      case AttendeeTimeAllocationDeclined(timeAllocationManagerId, attendeeId) =>
        val updateDeclined = timeAllocationManagerAttendeesDao.updateDeclined(timeAllocationManagerId, attendeeId, accepted = true)

        updateDeclined >> notifyEvent(eventMessage, "AttendeeTimeAllocationDeclined")

      case AllAttendeesTimeAllocationsAccepted(timeAllocationManagerId) =>
        val updateTimeAllocationManagerPreparedForAccepting = timeAllocationManagerDao.updatePreparedForAccepting(
          timeAllocationManagerId,
          preparedForAccepting = true
        )

//        val updateTimeAllocationManagerAttendeesAccepted = timeAllocationManagerAttendeesDao.updateAccepted(timeAllocationManagerId, attendeeId, accepted = true)


        updateTimeAllocationManagerPreparedForAccepting >> notifyEvent(eventMessage, "AllAttendeesTimeAllocationsAccepted")

      case AttendeeTimeDeallocated(timeAllocationManagerId, attendeeId) =>
        val remove = timeAllocationManagerAttendeesDao.remove(timeAllocationManagerId, attendeeId)

        remove >> notifyEvent(eventMessage, "AttendeeTimeDeallocated")
      case _ =>
        // TODO handle
        DBIOAction.successful(())
    }
  }
}

object CalendarTimeAllocationProjection {

  val notifyChannel = "events"

}