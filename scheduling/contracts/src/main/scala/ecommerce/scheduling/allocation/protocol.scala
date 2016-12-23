package ecommerce.scheduling.allocation

import java.time.{LocalDate, ZonedDateTime}

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId


sealed trait TimeAllocationCommand extends aggregate.Command {
  def allocationId: EntityId
  override def aggregateId = allocationId
}


case class CreateTimeAllocation(allocationId: EntityId, organizerId: EntityId) extends TimeAllocationCommand

//case class AllocateFullDayInterval(allocationId: EntityId, start: LocalDate, end: LocalDate) extends TimeAllocationCommand

case class AllocateDateTimeInterval(allocationId: EntityId, start: ZonedDateTime, end: ZonedDateTime) extends TimeAllocationCommand

case class RequestAttendeeTimeAllocation(allocationId: EntityId, attendeeId: EntityId) extends TimeAllocationCommand

case class AcceptAttendeeTimeAllocation(allocationId: EntityId, attendeeId: EntityId) extends TimeAllocationCommand

case class DeclineAttendeeTimeAllocation(allocationId: EntityId, attendeeId: EntityId) extends TimeAllocationCommand

//case class ApproveTimeAllocation(allocationId: EntityId, attendeeId: EntityId) extends TimeAllocationCommand


case class TimeAllocationCreated(allocationId: EntityId, organizerId: EntityId) extends TimeAllocationCommand

//case class FullDayIntervalAllocationRequested(allocationId: EntityId, start: LocalDate, end: LocalDate) extends TimeAllocationCommand

case class DateTimeIntervalAllocationRequested(allocationId: EntityId, start: ZonedDateTime, end: ZonedDateTime) extends TimeAllocationCommand

case class AttendeeTimeAllocationRequested(allocationId: EntityId, attendeeId: EntityId) extends TimeAllocationCommand

case class AttendeeTimeAllocationAccepted(allocationId: EntityId, attendeeId: EntityId) extends TimeAllocationCommand

case class AttendeeTimeAllocationDeclined(allocationId: EntityId, attendeeId: EntityId) extends TimeAllocationCommand
//case class TimeAllocationApproved(allocationId: EntityId, attendeeId: EntityId) extends TimeAllocationCommand
