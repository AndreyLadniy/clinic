package ecommerce.scheduling.reservation

import ecommerce.scheduling.DateTimeInterval
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate.{AggregateRoot, AggregateRootSupport, AggregateState}
import pl.newicom.dddd.eventhandling.EventPublisher
import pl.newicom.dddd.office.LocalOfficeId.fromRemoteId


object AttendeeReservation extends AggregateRootSupport {

  implicit val officeId = fromRemoteId[AttendeeReservation](AttendeeReservationOfficeId)

  case class State(reservations: List[(Long, Long)])
    extends AggregateState[State] {

    override def apply = {
      case ReservationAccepted(attendeeId, reservationId, start, end) =>
        copy(reservations = (start, end) :: reservations)
      case x: ReservationDeclined =>
        this
    }

  }

}

abstract class AttendeeReservation(val pc: PassivationConfig) extends AggregateRoot[AttendeeReservation.State, AttendeeReservation] {
  this: EventPublisher =>

  import AttendeeReservation.State

  override val factory: AggregateRootFactory = {
    case InvitingCreated(attendeeId) =>
      State(List.empty)
  }

  override def handleCommand: Receive = {
    case CreateInviting(attendeeId) =>
      if (initialized) {
        throw new RuntimeException(s"Inviting for $attendeeId already exists")
      } else {
        raise(InvitingCreated(attendeeId))
      }
    case ReserveInterval(attendeeId, reservationId, start, end) =>
//      if (state.reservations.exists(p => p.start.isBefore(interval.end) && p.end.isAfter(interval.start))) {
      if (state.reservations.exists(p => p._1 <= end && p._2 >= start)) {
        raise(ReservationDeclined(attendeeId, reservationId))
      } else {
        raise(ReservationAccepted(attendeeId, reservationId, start, end))
      }
  }
}