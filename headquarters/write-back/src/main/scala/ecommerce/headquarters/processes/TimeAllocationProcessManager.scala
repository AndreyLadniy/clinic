package ecommerce.headquarters.processes

import ecommerce.headquarters.app.HeadquartersConfiguration.department
import ecommerce.scheduling._
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate.{DomainEvent, EntityId}
import pl.newicom.dddd.process._
import pl.newicom.dddd.saga.ProcessConfig

//trait TimeAllocationProcessManagerOfficeListener {
//
//  implicit def officeListener[E <: Saga](implicit cs: CreationSupport, rf: ReceptorFactory): CoordinationOfficeListener[E] =
//    new CoordinationOfficeListener[E] {
//      override def officeStarted(office: Office): Unit = {
//        log.info("{} Office of {} Department- started up successfully.", office.id, office.department:Any)
//
//        super.officeStarted(office)
//      }
//    }
//
//}

object TimeAllocationProcessManager extends SagaSupport {
//  TimeAllocationProcessManagerOfficeListener {

  sealed trait CalendarTimeManagerStatus extends SagaState[CalendarTimeManagerStatus]

  case object New extends CalendarTimeManagerStatus

  case object RejectRequests extends CalendarTimeManagerStatus

  implicit object TimeHoldingProcessConfig extends ProcessConfig[TimeAllocationProcessManager]("CalendarTimeEvents", department) {
    def correlationIdResolver: PartialFunction[DomainEvent, EntityId] = {
      case CalendarCreated(calendarId, _) => calendarId
      case AttendeeTimeAllocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) => attendeeId
      case AttendeeTimeDeallocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) => attendeeId
      case AttendeeTimeReallocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) => attendeeId
      case CalendarTimeAllocated(calendarId, timeAllocationManagerId, organizerId, interval) => calendarId
      case CalendarTimeAllocatedFromQueue(calendarId, timeAllocationManagerId, _, _) => calendarId
      case CalendarTimeReallocated(calendarId, timeAllocationManagerId, organizerId, interval) => calendarId
      case CalendarTimeReallocatedFromQueue(calendarId, timeAllocationManagerId, organizerId, interval) => calendarId
      case CalendarTimeDeallocated(calendarId, timeAllocationManagerId) => calendarId
      case CalendarTimeDeallocatedFromQueue(calendarId, timeAllocationManagerId) => calendarId

//      case DeallocateAttendeeTimeErrorOccurred(timeAllocationManagerId, attendeeId, _) =>
//      case DeallocateCalendarTimeErrorOccurred(calendarId, _) => calendarId
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

    case _: CalendarCreated => New

    case _ => RejectRequests

  } andThen {

    case RejectRequests => {

      case AttendeeTimeAllocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) =>
        ⟶ (DeclineAttendeeCalendarTimeAllocation(timeAllocationManagerId, attendeeId))

      case AttendeeTimeReallocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) =>
        ⟶ (DeclineAttendeeCalendarTimeAllocation(timeAllocationManagerId, attendeeId))

      case AttendeeTimeDeallocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) =>
        ⟶ (AcceptAttendeeCalendarTimeDeallocation(timeAllocationManagerId, attendeeId))

      case _: CalendarCreated => New
    }

    case New => {

      //TimeAllocationManagerOfficeId events
      //On inviting attendee
      case AttendeeTimeAllocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) =>
        ⟶ (AllocateCalendarTime(attendeeId, timeAllocationManagerId, organizerId, Interval(start, end)))

      case AttendeeTimeDeallocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) =>
        ⟶ (DeallocateCalendarTime(attendeeId, timeAllocationManagerId))

        //On moving interval
      case AttendeeTimeReallocationRequested(timeAllocationManagerId, organizerId, attendeeId, start, end) =>
        ⟶ (ReallocateCalendarTime(attendeeId, timeAllocationManagerId, organizerId, Interval(start, end)))

      //CalendarTimeManagerOfficeId events

      //On allocation complete
      case CalendarTimeAllocated(calendarId, timeAllocationManagerId, organizerId, interval) =>
        ⟶ (AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId, calendarId))

      case CalendarTimeAllocatedFromQueue(calendarId, timeAllocationManagerId, _, _) =>
        ⟶ (AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId, calendarId))


      case CalendarTimeReallocated(calendarId, timeAllocationManagerId, organizerId, interval) =>
        ⟶ (AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId, calendarId))

      case CalendarTimeReallocatedFromQueue(calendarId, timeAllocationManagerId, organizerId, interval) =>
        ⟶ (AcceptAttendeeCalendarTimeAllocation(timeAllocationManagerId, calendarId))


      case CalendarTimeDeallocated(calendarId, timeAllocationManagerId) =>
        ⟶ (AcceptAttendeeCalendarTimeDeallocation(timeAllocationManagerId, calendarId))

      case CalendarTimeDeallocatedFromQueue(calendarId, timeAllocationManagerId) =>
        ⟶ (AcceptAttendeeCalendarTimeDeallocation(timeAllocationManagerId, calendarId))

//      case DeallocateCalendarTimeErrorOccurred(calendarId, timeAllocationManagerId) =>
//        ⟶ (AcceptAttendeeCalendarTimeDeallocation(timeAllocationManagerId, calendarId))

//      case alod.Processed(deliveryId, Failure(exception)) =>
//        println(exception.toString)
    }

  }

}
