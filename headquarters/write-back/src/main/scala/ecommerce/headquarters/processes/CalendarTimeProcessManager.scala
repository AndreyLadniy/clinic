package ecommerce.headquarters.processes

import ecommerce.headquarters.app.HeadquartersConfiguration.department
import ecommerce.headquarters.processes.CalendarTimeProcessManager.CalendarTimeManagerStatus
import ecommerce.scheduling.calendar.CalendarCreated
import ecommerce.scheduling.{AttendeeInvited, _}
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate.{DomainEvent, EntityId}
import pl.newicom.dddd.process._
import pl.newicom.dddd.saga.ProcessConfig

object CalendarTimeProcessManager extends SagaSupport {

  sealed trait CalendarTimeManagerStatus extends SagaState[CalendarTimeManagerStatus]

  case object New extends CalendarTimeManagerStatus

  implicit object TimeHoldingProcessConfig extends ProcessConfig[CalendarTimeProcessManager]("CalendarTimeHolding", department) {
    def correlationIdResolver: PartialFunction[DomainEvent, EntityId] = {
      case CalendarCreated(calendarId) => calendarId
      case AttendeeInvited(reservationId, attendeeId, start, end) => attendeeId
    }
  }

}

class CalendarTimeProcessManager(val pc: PassivationConfig) extends ProcessManager[CalendarTimeManagerStatus] {

  import CalendarTimeProcessManager._

  val officeId = TimeHoldingProcessConfig

  def processCollaborators = List(
    OrganizerReservationOfficeId
  )

  startWhen {

    case _: CalendarCreated => New(List.empty)

  } andThen {

    case New => {

      case AttendeeTimeAllocationRequested(calendarTimeAllocationId, organizerId, attendeeId, interval) =>
        ⟶ (AllocateCalendarTime(attendeeId, calendarTimeAllocationId, organizerId, interval))

      case CalendarTimeAllocationIntervalChanged(calendarTimeAllocationId, organizerId, attendees, interval) =>
        attendees.foreach(attendeeId =>
          ⟶ (AllocateCalendarTime(attendeeId, calendarTimeAllocationId, organizerId, interval))
        )

      case AttendeeTimeDeallocationRequested(calendarTimeAllocationId, organizerId, attendeeId, interval) =>
        ⟶ (DeallocateCalendarTime(attendeeId, calendarTimeAllocationId))

    }

  }

}
