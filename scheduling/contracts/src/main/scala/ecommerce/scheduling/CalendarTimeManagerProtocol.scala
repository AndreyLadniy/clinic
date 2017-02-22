package ecommerce.scheduling

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId


sealed trait CalendarTimeManagerCommand extends aggregate.Command {
  def calendarId: EntityId
  override def aggregateId: EntityId = calendarId
}

sealed trait CalendarTimeManagerEvent

case class AllocateCalendarTime(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerCommand

case class CalendarTimeAllocated(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerEvent

object CalendarTimeAllocated {
  def apply(command: AllocateCalendarTime): CalendarTimeAllocated = new CalendarTimeAllocated(command.calendarId, command.calendarTimeAllocationId, command.organizerId, command.interval)
}

case class CalendarTimeAllocationQueued(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerEvent

case class CalendarTimeAllocatedFromQueue(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerEvent



case class DeallocateCalendarTime(calendarId: EntityId, calendarTimeAllocationId: EntityId) extends CalendarTimeManagerCommand


case class ReallocateCalendarTime(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerCommand

case class CalendarTimeReallocated(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerEvent

case class CalendarTimeReallocationQueued(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerEvent

case class CalendarTimeReallocatedFromQueue(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerEvent

object CalendarTimeReallocatedFromQueue {
  def apply(command: ReallocateCalendarTime): CalendarTimeReallocatedFromQueue = new CalendarTimeReallocatedFromQueue(command.calendarId, command.calendarTimeAllocationId, command.organizerId, command.interval)
}

case class CalendarTimeReallocatedInQueue(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerEvent

object CalendarTimeReallocatedInQueue {
  def apply(command: ReallocateCalendarTime): CalendarTimeReallocatedInQueue = new CalendarTimeReallocatedInQueue(command.calendarId, command.calendarTimeAllocationId, command.organizerId, command.interval)
}


case class CalendarTimeDeallocated(calendarId: EntityId, calendarTimeAllocationId: EntityId) extends CalendarTimeManagerEvent

case class CalendarTimeDeallocatedFromQueue(calendarId: EntityId, calendarTimeAllocationId: EntityId) extends CalendarTimeManagerEvent

//case class CalendarTimeDeallocatedFromQueue(calendarId: EntityId, calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) extends CalendarTimeManagerEvent



case class AllocateCalendarTimeInterval(calendarId: EntityId, interval: Interval) extends CalendarTimeManagerCommand

case class TimeIntervalAllocated(calendarId: EntityId, interval: Interval) extends CalendarTimeManagerEvent

case class TimeIntervalAllocationDeclined(calendarId: EntityId) extends CalendarTimeManagerEvent


