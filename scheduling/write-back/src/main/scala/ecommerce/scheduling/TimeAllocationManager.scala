package ecommerce.scheduling

import java.time.ZonedDateTime

import ecommerce.scheduling.TimeAllocationManager.TimeAllocationManagerActions
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate._
import pl.newicom.dddd.eventhandling.EventPublisher
import pl.newicom.dddd.office.LocalOfficeId
import pl.newicom.dddd.office.LocalOfficeId.fromRemoteId

object TimeAllocationManager extends AggregateRootSupport {

  implicit val officeId: LocalOfficeId[TimeAllocationManager] = fromRemoteId[TimeAllocationManager](TimeAllocationManagerOfficeId)

  sealed trait TimeAllocationManagerActions extends AggregateActions[TimeAllocationManagerEvent, TimeAllocationManagerActions] {}

  implicit case object Uninitialized extends TimeAllocationManagerActions with Uninitialized[TimeAllocationManagerActions] {

    def actions: Actions =
      handleCommands {
        case CreateTimeAllocationManager(timeAllocationManagerId, organizerId, start, end) =>
          TimeAllocationManagerCreated(timeAllocationManagerId, organizerId, start, end)
      }
      .handleEvents {
        case TimeAllocationManagerCreated(_, organizerId, start, end) =>
          Created(organizerId, start, end, List.empty, List.empty, List.empty)
      }
  }

  case class Created(organizerId: EntityId, start: ZonedDateTime, end: ZonedDateTime, attendees: List[EntityId], allocationRequested: List[EntityId], deallocationRequested: List[EntityId]) extends TimeAllocationManagerActions {

    def actions: Actions =
      handleCommands{
        case AllocateAttendeeTime(timeAllocationManagerId, attendeeId) =>
          if (attendees.contains(attendeeId)) {
            throw new RuntimeException(s"TimeAllocationManager $timeAllocationManagerId contains $attendeeId attendee")
          } else {
            AttendeeTimeAllocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end)
          }
        case AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) =>
          if (!allocationRequested.contains(attendeeId)) {
            throw new RuntimeException(s"TimeAllocationManager $timeAllocationManagerId does not requests $attendeeId attendee time allocation")
          } else {
            if (allocationRequested.tail.isEmpty) {
              LastAttendeeTimeAllocated(timeAllocationManagerId, attendeeId)
            } else {
              AttendeeTimeAllocated(timeAllocationManagerId, attendeeId)
            }

          }
        case DeallocateAttendeeTime(timeAllocationManagerId, attendeeId) =>
          if (!attendees.contains(attendeeId)) {
            throw new RuntimeException(s"TimeAllocationManager $timeAllocationManagerId does not contain $attendeeId attendee")
          } else {
            AttendeeTimeDeallocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end)
          }
        case AcceptAttendeeCalendarTimeDeallocation(timeAllocationManagerId, attendeeId) =>
          if (!deallocationRequested.contains(attendeeId)) {
            throw new RuntimeException(s"TimeAllocationManager $timeAllocationManagerId does not requests $attendeeId attendee deallocation")
          } else {
            AttendeeTimeDeallocated(timeAllocationManagerId, attendeeId)
          }
        case MoveTimeAllocationManagerInterval(timeAllocationManagerId, startTo, endTo) =>
          if (startTo.isAfter(endTo)) {
            throw new RuntimeException(s"TimeAllocationManager $timeAllocationManagerId cannot apply anterval with start > end")
          } else {
            TimeAllocationManagerIntervalMoved(timeAllocationManagerId, organizerId, attendees, startTo, endTo) :: attendees.map(AttendeeTimeReallocationRequested(timeAllocationManagerId, organizerId, _, startTo, endTo))
          }
      }
      .handleEvents{
        case AttendeeTimeAllocationRequested(_, _, attendeeId, _, _) =>
          copy(attendees = attendeeId :: attendees, allocationRequested = attendeeId :: allocationRequested)
        case AttendeeTimeReallocationRequested(_, _, attendeeId, _, _) =>
          copy(
            allocationRequested = if (allocationRequested.contains(attendeeId)) {
              allocationRequested
            } else {
              attendeeId :: allocationRequested
            }
          )
        case AttendeeTimeAllocated(_, attendeeId) =>
          copy(allocationRequested = allocationRequested.filterNot(_ == attendeeId))
        case LastAttendeeTimeAllocated(_, attendeeId) =>
          copy(allocationRequested = List.empty)
        case AttendeeTimeDeallocationRequested(_, _, attendeeId, _, _) =>
          copy(deallocationRequested = attendeeId :: deallocationRequested)
        case AttendeeTimeDeallocated(_, attendeeId) =>
          copy(attendees = attendees.filterNot(_ == attendeeId), deallocationRequested = deallocationRequested.filterNot(_ == attendeeId))
        case TimeAllocationManagerIntervalMoved(_, _, _, startTo, endTo) =>
          copy(start = startTo, end = endTo)
      }
  }

}

abstract class TimeAllocationManager(val pc: PassivationConfig) extends AggregateRoot[TimeAllocationManagerEvent, TimeAllocationManagerActions, TimeAllocationManager] {
  this: EventPublisher =>

}
