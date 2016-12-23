package ecommerce.scheduling

import akka.cluster.sharding.ShardRegion.EntityId
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate.{AggregateRoot, AggregateRootSupport, AggregateState, DomainEvent}
import pl.newicom.dddd.eventhandling.EventPublisher
import pl.newicom.dddd.office.LocalOfficeId
import pl.newicom.dddd.office.LocalOfficeId.fromRemoteId

import scala.concurrent.duration._

object CalendarTimeAllocation extends AggregateRootSupport {

  implicit val officeId: LocalOfficeId[CalendarTimeAllocation] = fromRemoteId[CalendarTimeAllocation](CalendarTimeAllocationOfficeId)

  case class State(organizerId: EntityId, interval: Interval, attendees: List[EntityId])
    extends AggregateState[State] {

    override def apply: PartialFunction[DomainEvent, State] = {
      case AttendeeTimeAllocationRequested(_, _, attendeeId, _) =>
        copy(attendees = attendeeId :: attendees)
      case AttendeeTimeDeallocationRequested(_, _, attendeeId, _) =>
        copy(attendees = attendees.filterNot(_ == attendeeId))
      case CalendarTimeAllocationIntervalChanged(_, _, _, newInterval) =>
        copy(interval = newInterval)
    }

  }

}

abstract class CalendarTimeAllocation(val pc: PassivationConfig) extends AggregateRoot[CalendarTimeAllocation.State, CalendarTimeAllocation] {
  this: EventPublisher =>

  import CalendarTimeAllocation.State

  implicit val timeout: FiniteDuration = 1.minute

  override val factory: AggregateRootFactory = {
    case CalendarTimeAllocationCreated(_, organizerId, interval) =>
      State(organizerId, interval, List.empty)
  }


  override def handleCommand: Receive = {
    case CreateCalendarTimeAllocation(calendarTimeAllocationId, organizerId, interval) =>
      if (initialized) {
        throw new RuntimeException(s"CalendarTimeAllocation $calendarTimeAllocationId already exists")
      } else {
        raise(CalendarTimeAllocationCreated(calendarTimeAllocationId, organizerId, interval))
      }
    case AllocateAttendeeTime(calendarTimeAllocationId, attendeeId) =>
      if (state.attendees.contains(attendeeId)) {
        throw new RuntimeException(s"CalendarTimeAllocation contains $attendeeId attendee")
      } else {
        raise(AttendeeTimeAllocationRequested(calendarTimeAllocationId, state.organizerId, attendeeId, state.interval))
      }
    case DeallocateAttendeeTime(calendarTimeAllocationId, attendeeId) =>
      if (!state.attendees.contains(attendeeId)) {
        throw new RuntimeException(s"CalendarTimeAllocation does not contain $attendeeId attendee")
      } else {
        raise(AttendeeTimeDeallocationRequested(calendarTimeAllocationId, state.organizerId, attendeeId, state.interval))
      }
    case ChangeCalendarTimeAllocationInterval(calendarTimeAllocationId, interval) =>
      if (!initialized) {
        throw new RuntimeException(s"CalendarTimeAllocation $calendarTimeAllocationId  does not exists")
      } else {
        raise(CalendarTimeAllocationIntervalChanged(calendarTimeAllocationId, state.organizerId, state.attendees, interval))
      }
  }
}
