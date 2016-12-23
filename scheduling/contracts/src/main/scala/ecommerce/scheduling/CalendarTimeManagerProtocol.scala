package ecommerce.scheduling

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId


sealed trait CalendarTimeManagerCommand extends aggregate.Command {
  def calendarId: EntityId
  override def aggregateId: EntityId = calendarId
}

case class AllocateCalendarTime(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerCommand

case class DeallocateCalendarTime(calendarId: EntityId, calendarTimeAllocationId: EntityId) extends CalendarTimeManagerCommand

case class ReallocateCalendarTime(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerCommand


case class CalendarTimeAllocated(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval)

case class CalendarTimeAllocationQueued(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval)

case class CalendarTimeReallocated(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval)

case class CalendarTimeReallocationQueued(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval)

case class CalendarTimeDeallocated(calendarId: EntityId, calendarTimeAllocationId: EntityId)

case class CalendarTimeAllocatedFromQueue(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval)



case class AllocateCalendarTimeInterval(calendarId: EntityId, interval: Interval) extends CalendarTimeManagerCommand

case class TimeIntervalAllocated(calendarId: EntityId, interval: Interval)

case class TimeIntervalAllocationDeclined(calendarId: EntityId)


