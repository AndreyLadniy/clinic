package ecommerce.scheduling

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId

sealed trait CalendarTimeAllocationCommand extends aggregate.Command {
  def calendarTimeAllocationId: EntityId
  override def aggregateId: EntityId = calendarTimeAllocationId
}

case class CreateCalendarTimeAllocation(calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeAllocationCommand

case class AllocateAttendeeTime(calendarTimeAllocationId: EntityId, attendeeId: EntityId) extends CalendarTimeAllocationCommand

case class DeallocateAttendeeTime(calendarTimeAllocationId: EntityId, attendeeId: EntityId) extends CalendarTimeAllocationCommand

case class ChangeCalendarTimeAllocationInterval(calendarTimeAllocationId: EntityId, interval: Interval) extends CalendarTimeAllocationCommand


case class CalendarTimeAllocationCreated(calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval)

case class AttendeeTimeAllocationRequested(calendarTimeAllocationId: EntityId, organizerId: EntityId, attendeeId: EntityId, interval: Interval)

case class AttendeeTimeDeallocationRequested(calendarTimeAllocationId: EntityId, organizerId: EntityId, attendeeId: EntityId, interval: Interval)

case class CalendarTimeAllocationIntervalChanged(calendarTimeAllocationId: EntityId, organizerId: EntityId, attendees: List[EntityId], interval: Interval)
