package ecommerce.scheduling

import java.util.Date

import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate.{AggregateRoot, AggregateRootSupport, AggregateState, EntityId}
import pl.newicom.dddd.eventhandling.EventPublisher
import pl.newicom.dddd.office.LocalOfficeId.fromRemoteId

object OrganizerReservation extends AggregateRootSupport {

  implicit val officeId = fromRemoteId[OrganizerReservation](OrganizerReservationOfficeId)

  case class State(
      organizer: EntityId,
      interval: (Long, Long),
      attendees: List[EntityId],
      createDate: Date)
    extends AggregateState[State] {

    override def apply = {
      case AttendeeInvited(_, attendee, _, _) =>
        copy(attendees = attendee :: attendees)
//      case AttendeeInvited(_, attendee, _) =>
//        copy(attendees = ReservationAttendee(attendee, ResponseStatus.NeedsAction) :: attendees)
//      case AttendeeAcceptInvitation(_, attendee) =>
//        val newAttendees = attendees.find(_.attendee == attendee) match {
//          case Some(attendeeReservation) =>
//            val index = attendees.indexOf(attendeeReservation)
//            attendees.updated(index, attendeeReservation.copy(responseStatus = ResponseStatus.Accepted))
//          case None =>
//            ReservationAttendee(attendee, ResponseStatus.Accepted) :: attendees
//        }
//        copy(attendees = newAttendees)
//      case AttendeeDeclineInvitation(_, attendee) =>
//        val newAttendees = attendees.find(_.attendee == attendee) match {
//          case Some(attendeeReservation) =>
//            val index = attendees.indexOf(attendeeReservation)
//            attendees.updated(index, attendeeReservation.copy(responseStatus = ResponseStatus.Declined))
//          case None =>
//            ReservationAttendee(attendee, ResponseStatus.Declined) :: attendees
//        }
//        copy(attendees = newAttendees)
//
//      case ReservationConfirmed(_, _, _) =>
//        copy(status = Confirmed)
//
//      case ReservationCanceled(_) =>
//        copy(status = ReservationStatus.Canceled)
//
//      case ReservationClosed(_) =>
//        copy(status = Closed)
    }

//    def totalAmount: Option[Money] = {
//      items.foldLeft(Option.empty[Money]) {(mOpt, item) => mOpt.flatMap(m => item.product.price.map(_ + m)) }
//    }
  }

}

abstract class OrganizerReservation(val pc: PassivationConfig) extends AggregateRoot[OrganizerReservation.State, OrganizerReservation] {
  this: EventPublisher =>

  import OrganizerReservation.State

  override val factory: AggregateRootFactory = {
    case ReservationCreated(reservationId, organizer, start, end) =>
      State(organizer, (start, end), attendees = List.empty, createDate = new Date)
  }

  override def handleCommand: Receive = {
    case CreateReservation(reservationId, organizer, start, end) =>
      if (initialized) {
        throw new RuntimeException(s"Reservation $reservationId already exists")
      } else {
        raise(ReservationCreated(reservationId, organizer, start, end))
      }
    case InviteAttendee(reservationId, attendee) =>
      if (state.attendees.contains(attendee)) {
        throw new RuntimeException(s"Attendee $attendee in list already")
      } else {
        raise(AttendeeInvited(reservationId, attendee, state.interval._1, state.interval._2))
      }
//    case AcceptAttendeeInvitation(reservationId, attendee) =>
//      if (!state.attendees.exists(_.attendee == attendee)) {
//        throw new RuntimeException(s"Attendee $attendee not exists in list")
//      } else {
//        raise(AttendeeAcceptInvitation(reservationId, attendee))
//      }
//    case DeclineAttendeeInvitation(reservationId, attendee) =>
//      if (!state.attendees.exists(_.attendee == attendee)) {
//        throw new RuntimeException(s"Attendee $attendee not exists in list")
//      } else {
//        raise(AttendeeDeclineInvitation(reservationId, attendee))
//      }
//    case ReserveProduct(reservationId, product, quantity) =>
//      if (state.status eq Closed) {
//        throw new RuntimeException(s"Reservation $reservationId is closed")
//      } else {
//        raise(ProductReserved(reservationId, product, quantity))
//      }
//
//    case ConfirmReservation(reservationId) =>
//      if ((state.status eq Closed) || (state.status eq Canceled)) {
//        throw new RuntimeException(s"Reservation $reservationId is ${state.status}")
//      } else {
//        raise(ReservationConfirmed(reservationId, state.customerId, state.totalAmount))
//      }
//
    case CancelReservation(reservationId) =>
      raise(ReservationCanceled(reservationId))
//
//    case CloseReservation(reservationId) =>
//      raise(ReservationClosed(reservationId))
  }
}
