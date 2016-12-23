package ecommerce.scheduling.invitation

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId

sealed trait InvitationCommand extends aggregate.Command {
  def calendarId: EntityId
  override def aggregateId: EntityId = calendarId
}

case class CreateInvitationManager(calendarId: EntityId) extends InvitationCommand

case class ReserveTimeInterval(calendarId: EntityId, reservationId: EntityId, start: Long, end: Long) extends InvitationCommand

case class BookTimeInterval(calendarId: EntityId, eventId: EntityId, start: Long, end: Long) extends InvitationCommand


case class InvitationManagerCreated(calendarId: EntityId)

case class TimeIntervalReserved(calendarId: EntityId, reservationId: EntityId, start: Long, end: Long)

case class TimeIntervalReservationDeclined(calendarId: EntityId, reservationId: EntityId)

case class TimeIntervalBooked(calendarId: EntityId, eventId: EntityId, start: Long, end: Long)

case class TimeIntervalBookingDeclined(calendarId: EntityId, eventId: EntityId)