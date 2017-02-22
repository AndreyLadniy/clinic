package ecommerce.headquarters.processes

import ecommerce.headquarters.app.HeadquartersConfiguration.department
import pl.newicom.dddd.aggregate.{DomainEvent, EntityId}
import ecommerce.scheduling._
import ecommerce.scheduling.calendar.{CalendarCreated, CalendarOfficeId}
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.process._
import pl.newicom.dddd.saga.ProcessConfig

object TimeAllocationProcessManager extends SagaSupport {

  sealed trait CalendarTimeManagerStatus extends SagaState[CalendarTimeManagerStatus]

  case object New extends CalendarTimeManagerStatus

  implicit object TimeHoldingProcessConfig extends ProcessConfig[TimeAllocationProcessManager]("calendarTimeEvents", department) {
    def correlationIdResolver: PartialFunction[DomainEvent, EntityId] = {
      case CalendarCreated(calendarId) => calendarId
      case AttendeeTimeAllocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) => attendeeId
      case AttendeeTimeDeallocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) => attendeeId
      case CalendarTimeAllocated(calendarId, timeAllocationManagerId, organizerId, interval) => calendarId
    }
  }

}

import ecommerce.headquarters.processes.TimeAllocationProcessManager._

class TimeAllocationProcessManager(val pc: PassivationConfig) extends ProcessManager[CalendarTimeManagerStatus] {

  val officeId = TimeHoldingProcessConfig

  def processCollaborators = List(
    CalendarOfficeId, TimeAllocationManagerOfficeId, CalendarTimeManagerOfficeId
  )

  startWhen {

    case _: CalendarCreated => New(List.empty)

  } andThen {

    case New => {

      //On inviting attendee
      case AttendeeTimeAllocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) =>
        ⟶ (AllocateCalendarTime(attendeeId, timeAllocationManagerId, organizerId, Interval(start, end)))

        //On moving interval
      case AttendeeTimeReallocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) =>
        ⟶ (ReallocateCalendarTime(attendeeId, timeAllocationManagerId, organizerId, Interval(start, end)))

      //On allocation complete
      case CalendarTimeAllocated(calendarId, timeAllocationManagerId, organizerId, interval) =>
        ⟶ (AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId, calendarId))

//      case TimeAllocationManagerIntervalMoved(calendarTimeAllocationId, organizerId, attendees, start, end) =>
//        attendees.foreach(attendeeId =>
//          ⟶ (RequestAttendeeTimeAllocation(calendarTimeAllocationId, attendeeId))
//        )

      //On deallocation time (removing attendee from time allocation manager)
      case AttendeeTimeDeallocationRequested(calendarTimeAllocationId, organizerId, attendeeId, start, end) =>
        ⟶ (DeallocateCalendarTime(attendeeId, calendarTimeAllocationId))

    }

  }

}
