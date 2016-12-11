package ecommerce.scheduling.reservation

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId

sealed trait Command extends aggregate.Command {
  def attendeeId: EntityId
  override def aggregateId: EntityId = attendeeId
}

case class CreateInviting(attendeeId: EntityId) extends Command

case class ReserveInterval(attendeeId: EntityId, reservationId: EntityId, start: Long, end: Long) extends Command

case class CancelIntervalReservation(attendeeId: EntityId, reservationId: EntityId, start: Long, end: Long) extends Command


case class InvitingCreated(attendeeId: EntityId)

case class ReservationAccepted(attendeeId: EntityId, reservationId: EntityId, start: Long, end: Long)

case class ReservationDeclined(attendeeId: EntityId, reservationId: EntityId)

case class ReservationCanceled(attendeeId: EntityId, reservationId: EntityId, start: Long, end: Long)
