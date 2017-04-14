package ecommerce.scheduling

import java.time.OffsetDateTime

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId

sealed trait TimeAllocationManagerCommand extends aggregate.Command {
  def timeAllocationManagerId: EntityId
  override def aggregateId: EntityId = timeAllocationManagerId
}

case class CreateTimeAllocationManager(timeAllocationManagerId: EntityId, organizerId: EntityId, start: OffsetDateTime, end: OffsetDateTime) extends TimeAllocationManagerCommand


case class AllocateAttendeeTime(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerCommand

case class AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerCommand

case class DeclineAttendeeCalendarTimeAllocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerCommand

case class DeallocateAttendeeTime(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerCommand

case class AcceptAttendeeCalendarTimeDeallocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerCommand

case class MoveTimeAllocationManagerInterval(timeAllocationManagerId: EntityId, start: OffsetDateTime, end: OffsetDateTime) extends TimeAllocationManagerCommand


sealed trait TimeAllocationManagerEvent

case class TimeAllocationManagerCreated(timeAllocationManagerId: EntityId, organizerId: EntityId, start: OffsetDateTime, end: OffsetDateTime) extends TimeAllocationManagerEvent


case class AttendeeTimeAllocationRequested(timeAllocationManagerId: EntityId, organizerId: EntityId, attendeeId: EntityId, start: OffsetDateTime, end: OffsetDateTime) extends TimeAllocationManagerEvent

case class AttendeeTimeAllocationAccepted(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerEvent

case class AllAttendeesTimeAllocationsAccepted(timeAllocationManagerId: EntityId) extends TimeAllocationManagerEvent

//  case class LastAttendeeTimeAllocated(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerEvent

case class AttendeeTimeAllocationDeclined(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerEvent

case class AttendeeTimeDeallocationRequested(timeAllocationManagerId: EntityId, organizerId: EntityId, attendeeId: EntityId, start: OffsetDateTime, end: OffsetDateTime) extends TimeAllocationManagerEvent

case class AttendeeTimeDeallocationAccepted(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerEvent

case class TimeAllocationManagerIntervalMoved(timeAllocationManagerId: EntityId, organizerId: EntityId, attendees: List[EntityId], start: OffsetDateTime, end: OffsetDateTime) extends TimeAllocationManagerEvent

case class AttendeeTimeReallocationRequested(timeAllocationManagerId: EntityId, organizerId: EntityId, attendeeId: EntityId, start: OffsetDateTime, end: OffsetDateTime) extends TimeAllocationManagerEvent
