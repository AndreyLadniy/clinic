package ecommerce.scheduling

import java.time.ZonedDateTime

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId


sealed trait TimeAllocationManagerCommand extends aggregate.Command {
  def timeAllocationManagerId: EntityId
  override def aggregateId: EntityId = timeAllocationManagerId
}

sealed trait TimeAllocationManagerEvent

case class CreateTimeAllocationManager(timeAllocationManagerId: EntityId, organizerId: EntityId, start: ZonedDateTime, end: ZonedDateTime) extends TimeAllocationManagerCommand

case class TimeAllocationManagerCreated(timeAllocationManagerId: EntityId, organizerId: EntityId, start: ZonedDateTime, end: ZonedDateTime) extends TimeAllocationManagerEvent


case class AllocateAttendeeTime(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerCommand

//case class AttendeeTimeAllocationQueued(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerEvent

//case class RequestAttendeeTimeAllocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerCommand

case class AttendeeTimeAllocationRequested(timeAllocationManagerId: EntityId, organizerId: EntityId, attendeeId: EntityId, start: ZonedDateTime, end: ZonedDateTime) extends TimeAllocationManagerEvent


case class AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerCommand

case class AttendeeTimeAllocated(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerEvent

case class LastAttendeeTimeAllocated(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerEvent


case class DeallocateAttendeeTime(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerCommand

case class AttendeeTimeDeallocationRequested(timeAllocationManagerId: EntityId, organizerId: EntityId, attendeeId: EntityId, start: ZonedDateTime, end: ZonedDateTime) extends TimeAllocationManagerEvent


case class AcceptAttendeeCalendarTimeDeallocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerCommand

case class AttendeeTimeDeallocated(timeAllocationManagerId: EntityId, attendeeId: EntityId) extends TimeAllocationManagerEvent


case class MoveTimeAllocationManagerInterval(timeAllocationManagerId: EntityId, start: ZonedDateTime, end: ZonedDateTime) extends TimeAllocationManagerCommand

case class TimeAllocationManagerIntervalMoved(timeAllocationManagerId: EntityId, organizerId: EntityId, attendees: List[EntityId], start: ZonedDateTime, end: ZonedDateTime) extends TimeAllocationManagerEvent

case class AttendeeTimeReallocationRequested(timeAllocationManagerId: EntityId, organizerId: EntityId, attendeeId: EntityId, start: ZonedDateTime, end: ZonedDateTime) extends TimeAllocationManagerEvent
