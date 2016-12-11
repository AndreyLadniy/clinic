package ecommerce.scheduling

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId

sealed trait Command extends aggregate.Command {
  def reservationId: EntityId
  override def aggregateId = reservationId
}

case class CreateReservation(reservationId: EntityId, organizer: EntityId, start: Long, end: Long) extends Command

case class InviteAttendee(reservationId: EntityId, attendee: EntityId) extends Command

case class CancelReservation(reservationId: EntityId) extends Command

//case class AcceptAttendeeInvitation(reservationId: EntityId, attendee: Attendee) extends Command
//
//case class DeclineAttendeeInvitation(reservationId: EntityId, attendee: Attendee) extends Command
//
case class ReservationCreated(reservationId: EntityId, organizer: EntityId, start: Long, end: Long)
//
case class AttendeeInvited(reservationId: EntityId, attendee: EntityId, start: Long, end: Long)

//case class AttendeeDeclineInvitation(reservationId: EntityId, attendee: Attendee)
//
//case class AttendeeAcceptInvitation(reservationId: EntityId, attendee: Attendee)
//
case class ReservationCanceled(reservationId: EntityId)

//object ReservationStatus extends Enumeration {
//
//  type ReservationStatus = Value
//
//  val New, Accepted, Declined, Canceled = Value
//
//}
//
//object ResponseStatus extends Enumeration {
//
//  type ResponseStatus = Value
//
//  val NeedsAction, Declined, Tentative, Accepted = Value
//
//}
//
//case class ReservationAttendee(attendee: Attendee, responseStatus: ResponseStatus)