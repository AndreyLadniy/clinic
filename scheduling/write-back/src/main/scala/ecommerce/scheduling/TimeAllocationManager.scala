package ecommerce.scheduling

import java.time.OffsetDateTime

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
          Created(organizerId, start, end, List.empty, List.empty)
      }
  }

//  case class Allocated(organizerId: EntityId, start: OffsetDateTime, end: OffsetDateTime, attendees: List[EntityId], deallocationRequested: List[EntityId]) extends TimeAllocationManagerActions {
//
//    override def actions = {
//
//    }
//
//  }

  case class Attendee(attendeeId: EntityId, responseStatus: AttendeeResponseStatus)


  sealed trait AttendeeResponseStatus

  case object AttendeeNeedsAction extends AttendeeResponseStatus

  case object AttendeeAccepted extends AttendeeResponseStatus

  case object AttendeeDeclined extends AttendeeResponseStatus

  case class Created(organizerId: EntityId, start: OffsetDateTime, end: OffsetDateTime, attendees: List[Attendee], deallocationRequested: List[EntityId]) extends TimeAllocationManagerActions {

    def actions: Actions =
      handleCommands{
        case AllocateAttendeeTime(timeAllocationManagerId, attendeeId) if attendees.exists(_.attendeeId == attendeeId) =>
//          AllocateAttendeeTimeErrorOccurred(timeAllocationManagerId, attendeeId, s"TimeAllocationManager $timeAllocationManagerId contains $attendeeId attendee")
          error(s"TimeAllocationManager $timeAllocationManagerId contains $attendeeId attendee")
        case AllocateAttendeeTime(timeAllocationManagerId, attendeeId) =>
          AttendeeTimeAllocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end)

        case AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) if !attendees.exists(attendee => attendee.attendeeId == attendeeId && attendee.responseStatus == AttendeeNeedsAction) =>
          error(s"TimeAllocationManager $timeAllocationManagerId does not requests $attendeeId attendee time allocation")

        case AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) if attendees.filter(_.responseStatus != AttendeeAccepted).tail.isEmpty =>
          List(AttendeeTimeAllocationAccepted(timeAllocationManagerId, attendeeId), AllAttendeesTimeAllocationsAccepted(timeAllocationManagerId))
//          LastAttendeeTimeAllocated(timeAllocationManagerId, attendeeId)

        case AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) =>
          AttendeeTimeAllocationAccepted(timeAllocationManagerId, attendeeId)

        case DeclineAttendeeCalendarTimeAllocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) if !attendees.exists(attendee => attendee.attendeeId == attendeeId && attendee.responseStatus == AttendeeNeedsAction) =>
          error(s"TimeAllocationManager $timeAllocationManagerId does not requests $attendeeId attendee time allocation")

        case DeclineAttendeeCalendarTimeAllocation(timeAllocationManagerId: EntityId, attendeeId: EntityId) =>
          AttendeeTimeAllocationDeclined(timeAllocationManagerId, attendeeId)

        case DeallocateAttendeeTime(timeAllocationManagerId, attendeeId) if !attendees.exists(_.attendeeId == attendeeId) =>
//          DeallocateAttendeeTimeErrorOccurred(timeAllocationManagerId, attendeeId, s"TimeAllocationManager $timeAllocationManagerId does not contain $attendeeId attendee")
          error(s"TimeAllocationManager $timeAllocationManagerId does not contain $attendeeId attendee")

        case DeallocateAttendeeTime(timeAllocationManagerId, attendeeId) if deallocationRequested.contains(attendeeId) =>
//          DeallocateAttendeeTimeErrorOccurred(timeAllocationManagerId, attendeeId, s"TimeAllocationManager $timeAllocationManagerId contains $attendeeId attendee request for deallocation")
          error(s"TimeAllocationManager $timeAllocationManagerId contains $attendeeId attendee request for deallocation")

        case DeallocateAttendeeTime(timeAllocationManagerId, attendeeId) =>
          AttendeeTimeDeallocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end)

        case AcceptAttendeeCalendarTimeDeallocation(timeAllocationManagerId, attendeeId) if !deallocationRequested.contains(attendeeId) =>
          error(s"TimeAllocationManager $timeAllocationManagerId does not requests $attendeeId attendee deallocation")

        case AcceptAttendeeCalendarTimeDeallocation(timeAllocationManagerId, attendeeId) =>
          AttendeeTimeDeallocated(timeAllocationManagerId, attendeeId)

        case MoveTimeAllocationManagerInterval(timeAllocationManagerId, startTo, endTo) if startTo.isAfter(endTo) =>
          error(s"TimeAllocationManager $timeAllocationManagerId cannot apply interval with start > end")

        case MoveTimeAllocationManagerInterval(timeAllocationManagerId, startTo, endTo) =>
          TimeAllocationManagerIntervalMoved(timeAllocationManagerId, organizerId, attendees.map(_.attendeeId), startTo, endTo) :: attendees.map(attendee => AttendeeTimeReallocationRequested(timeAllocationManagerId, organizerId, attendee.attendeeId, startTo, endTo))

          //TODO Set prepared on last accepted and after remove with all other accepted
      }
      .handleEvents{
        case AttendeeTimeAllocationRequested(_, _, attendeeId, _, _) =>
          copy(attendees = Attendee(attendeeId, AttendeeNeedsAction) :: attendees.filterNot(_.attendeeId == attendeeId))
        case AttendeeTimeReallocationRequested(_, _, attendeeId, _, _) =>
          copy(attendees = Attendee(attendeeId, AttendeeNeedsAction) :: attendees.filterNot(_.attendeeId == attendeeId))
        case AttendeeTimeAllocationAccepted(_, attendeeId) =>
          copy(attendees = Attendee(attendeeId, AttendeeAccepted) :: attendees.filterNot(_.attendeeId == attendeeId))
//        case LastAttendeeTimeAllocated(_, attendeeId) =>
//          copy(attendees = Attendee(attendeeId, AttendeeAccepted) :: attendees.filterNot(_.attendeeId == attendeeId))
        case AttendeeTimeAllocationDeclined(_, attendeeId) =>
          copy(attendees = Attendee(attendeeId, AttendeeDeclined) :: attendees.filterNot(_.attendeeId == attendeeId))
        case AttendeeTimeDeallocationRequested(_, _, attendeeId, _, _) =>
          copy(deallocationRequested = attendeeId :: deallocationRequested)
        case AttendeeTimeDeallocated(_, attendeeId) =>
          copy(attendees = attendees.filterNot(_.attendeeId == attendeeId), deallocationRequested = deallocationRequested.filterNot(_ == attendeeId))
        case TimeAllocationManagerIntervalMoved(_, _, _, startTo, endTo) =>
          copy(start = startTo, end = endTo)
        case AllAttendeesTimeAllocationsAccepted(_) =>
          this
      }
  }

}

abstract class TimeAllocationManager(val pc: PassivationConfig) extends AggregateRoot[TimeAllocationManagerEvent, TimeAllocationManagerActions, TimeAllocationManager] {
  this: EventPublisher =>

}
