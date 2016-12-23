package ecommerce.scheduling

import akka.cluster.sharding.ShardRegion.EntityId
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate.{AggregateRoot, AggregateRootSupport, AggregateState, DomainEvent}
import pl.newicom.dddd.eventhandling.EventPublisher
import pl.newicom.dddd.office.LocalOfficeId
import pl.newicom.dddd.office.LocalOfficeId.fromRemoteId

import scala.concurrent.duration._

object CalendarTimeManager extends AggregateRootSupport {

  implicit val officeId: LocalOfficeId[CalendarTimeManager] = fromRemoteId[CalendarTimeManager](CalendarTimeManagerOfficeId)

  case class Allocation(calendarTimeAllocationId: EntityId, organizerId: EntityId, interval: Interval) {
    def hasIntersect(other: Allocation): Boolean = interval.hasIntersect(other.interval)
  }

  case class State(allocatedTime: List[Allocation], allocationTimeQueue: List[Allocation])
    extends AggregateState[State] {

    override def apply: PartialFunction[DomainEvent, State] = {
      case CalendarTimeAllocated(_, calendarTimeAllocationId, organizerId, interval) =>
        copy(
          allocatedTime = Allocation(calendarTimeAllocationId, organizerId, interval) :: allocatedTime
        )
      case CalendarTimeAllocationQueued(_, calendarTimeAllocationId, organizerId, interval) =>
        copy(
          allocationTimeQueue = Allocation(calendarTimeAllocationId, organizerId, interval) :: allocationTimeQueue
        )
      case CalendarTimeDeallocated(_, calendarTimeAllocationId) =>
        copy(
          allocatedTime = allocatedTime.filterNot(_.calendarTimeAllocationId == calendarTimeAllocationId),
          allocationTimeQueue = allocationTimeQueue.filterNot(_.calendarTimeAllocationId == calendarTimeAllocationId)
        )
      case CalendarTimeAllocatedFromQueue(_, calendarTimeAllocationId, organizerId, interval) =>
        copy(
          allocatedTime = Allocation(calendarTimeAllocationId, organizerId, interval) :: allocatedTime,
          allocationTimeQueue = allocationTimeQueue.filterNot(_.calendarTimeAllocationId == calendarTimeAllocationId)
        )
      case CalendarTimeReallocated(_, calendarTimeAllocationId, organizerId, interval) =>
        copy(
          allocatedTime = Allocation(calendarTimeAllocationId, organizerId, interval) :: allocatedTime.filterNot(_.calendarTimeAllocationId == calendarTimeAllocationId)
        )
      case CalendarTimeReallocationQueued(_, calendarTimeAllocationId, organizerId, interval) =>
        copy(
          allocatedTime = allocatedTime.filterNot(_.calendarTimeAllocationId == calendarTimeAllocationId),
          allocationTimeQueue = Allocation(calendarTimeAllocationId, organizerId, interval) :: allocationTimeQueue
        )
    }

  }

}

abstract class CalendarTimeManager(val pc: PassivationConfig) extends AggregateRoot[CalendarTimeManager.State, CalendarTimeManager] {
  this: EventPublisher =>

  import CalendarTimeManager.{Allocation, State}

  implicit val timeout: FiniteDuration = 1.minute

  override val factory: AggregateRootFactory = {
    case CalendarTimeAllocated(_, calendarTimeAllocationId, organizerId, interval) =>
      State(List(Allocation(calendarTimeAllocationId, organizerId, interval)), List.empty)
  }


  override def handleCommand: Receive = {
    case AllocateCalendarTime(calendarId, calendarTimeAllocationId, organizerId, interval) =>
      if (state.allocatedTime.exists(p => interval.hasIntersect(p.interval))) {
        raise(CalendarTimeAllocationQueued(calendarId, calendarTimeAllocationId, organizerId, interval))
      } else {
        raise(CalendarTimeAllocated(calendarId, calendarTimeAllocationId, organizerId, interval))
      }

    case DeallocateCalendarTime(calendarId, calendarTimeAllocationId) =>
      raise(CalendarTimeDeallocated(calendarId, calendarTimeAllocationId))

      val (deallocatedCalendarTime, resultAllocatedTime) = state.allocatedTime.partition(p => p.calendarTimeAllocationId == calendarTimeAllocationId)

      deallocatedCalendarTime.foreach(allocation =>
        state.allocationTimeQueue.filter(allocation.hasIntersect).foreach(queuedAllocation =>
          if (!resultAllocatedTime.exists(queuedAllocation.hasIntersect)) {
            raise(CalendarTimeAllocatedFromQueue(calendarId, queuedAllocation.calendarTimeAllocationId, queuedAllocation.organizerId, queuedAllocation.interval))
          }
        )
      )

    case ReallocateCalendarTime(calendarId, calendarTimeAllocationId, organizerId, interval) =>
      val (deallocatedCalendarTime, resultAllocatedTime) = state.allocatedTime.partition(p => p.calendarTimeAllocationId == calendarTimeAllocationId)

      if (resultAllocatedTime.exists(p => interval.hasIntersect(p.interval))) {
        raise(CalendarTimeReallocationQueued(calendarId, calendarTimeAllocationId, organizerId, interval))
      } else {
        raise(CalendarTimeReallocated(calendarId, calendarTimeAllocationId, organizerId, interval))
      }

  }
}
