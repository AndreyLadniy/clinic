package ecommerce.scheduling.view

import java.sql.{Date, Timestamp}

import ecommerce.scheduling.{TimeAllocationManagerCreated, TimeAllocationManagerIntervalMoved}
import pl.newicom.dddd.messaging.event.OfficeEventMessage
import pl.newicom.dddd.view.sql.Projection
import pl.newicom.dddd.view.sql.Projection.ProjectionAction
import slick.dbio.Effect.Write

import scala.concurrent.ExecutionContext

class CalendarTimeAllocationProjection(calendarTimeAllocationDao: CalendarTimeAllocationDao, attendeeCalendarTimeAllocationDao: AttendeeCalendarTimeAllocationDao)(implicit ec: ExecutionContext) extends Projection {

  override def consume(eventMessage: OfficeEventMessage): ProjectionAction[Write] = {
    eventMessage.event match {

      case TimeAllocationManagerCreated(calendarTimeAllocationId, organizerId, start, end) =>
        val newView = CalendarTimeAllocationView(calendarTimeAllocationId, organizerId, Timestamp.from(start.toInstant), Timestamp.from(end.toInstant))
        calendarTimeAllocationDao.createOrUpdate(newView)

      case TimeAllocationManagerIntervalMoved(calendarTimeAllocationId, organizerId, attendees, start, end) =>
        val updateCalendarTimeAllocationInterval = calendarTimeAllocationDao.updateInterval(
          calendarTimeAllocationId,
          Timestamp.from(start.toInstant),
          Timestamp.from(end.toInstant)
        )

        val updateAttendeeCalendarTimeAllocation = attendeeCalendarTimeAllocationDao.updateInterval(
          calendarTimeAllocationId,
          Timestamp.from(start.toInstant),
          Timestamp.from(end.toInstant)
        )

        updateCalendarTimeAllocationInterval >> updateAttendeeCalendarTimeAllocation

      //      case ReservationConfirmed(id, clientId, _) =>
//          dao.updateStatus(id, Confirmed)
//
//      case ReservationCanceled(id) =>
//        dao.updateStatus(id, Canceled)
//
//      case ReservationClosed(id) =>
//        dao.updateStatus(id, Closed)
//
//      case ProductReserved(id, product, quantity) =>
//        // TODO handle
//        DBIOAction.successful(())
    }
  }
}