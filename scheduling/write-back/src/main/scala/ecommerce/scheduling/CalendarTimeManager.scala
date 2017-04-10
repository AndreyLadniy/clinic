package ecommerce.scheduling

import java.time.OffsetDateTime

import ecommerce.scheduling.CalendarTimeManager.CalendarTimeManagerActions
import ecommerce.scheduling.timeline.{AllocatedTimeInterval, AllocationsTimeLine, TimeInterval}
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate._
import pl.newicom.dddd.eventhandling.EventPublisher
import pl.newicom.dddd.office.LocalOfficeId
import pl.newicom.dddd.office.LocalOfficeId.fromRemoteId

import scala.language.higherKinds


object CalendarTimeManager extends aggregate.AggregateRootSupport {

  implicit val officeId: LocalOfficeId[CalendarTimeManager] = fromRemoteId[CalendarTimeManager](CalendarTimeManagerOfficeId)

  sealed trait CalendarTimeManagerActions extends AggregateActions[CalendarTimeManagerEvent, CalendarTimeManagerActions] {}

  implicit case object Uninitialized extends CalendarTimeManagerActions with Uninitialized[CalendarTimeManagerActions] {
    def actions: _root_.ecommerce.scheduling.CalendarTimeManager.Uninitialized.Actions =
      handleCommands{
        case AllocateCalendarTime(calendarId, timeAllocationManagerId, organizerId, interval) =>
            CalendarTimeAllocated(calendarId, timeAllocationManagerId, organizerId, interval)
      }
      .handleEvents{
        case CalendarTimeAllocated(_, timeAllocationManagerId, organizerId, interval) =>
          Active(AllocationsTimeLine().add(AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId)), List.empty)
      }
  }

  def view[A <: TimeInterval](start: Option[OffsetDateTime], end: Option[OffsetDateTime], timeLine: List[A]): List[A] = {
    timeLine.filter(timeInterval => start.forall(!_.isAfter(timeInterval.start)) && end.forall(!_.isBefore(timeInterval.end)))
  }

  def allocate[A <: TimeInterval](timeLine: List[A], forAllocation: List[A]): List[A] = {
    forAllocation
      .view
//      .filter(timeInterval => start.forall(!_.isAfter(timeInterval.start)) && end.forall(!_.isBefore(timeInterval.end)) && !timeLine.exists(_.hasIntersect(timeInterval.start, timeInterval.end)))
      .filterNot(timeInterval => timeLine.exists(_.hasIntersect(timeInterval)))
      .foldLeft(List.empty[A]){
        case (acc, el) if acc.exists(_.hasIntersect(el.start, el.end)) => acc
        case (acc, el) => el :: acc
      }
  }

  case class Active(allocationsTimeLine: AllocationsTimeLine, allocationTimeQueue: List[AllocatedTimeInterval]) extends CalendarTimeManagerActions {

    def allocateActions: Actions = {

      def containsTimeAllocationManager(timeAllocationManagerId: EntityId) =
        allocationsTimeLine.hasAllocatedTimeIntervalByAllocationTimeManagerId(timeAllocationManagerId) || allocationTimeQueue.exists(_.timeAllocationManagerId == timeAllocationManagerId)

      handleCommands{
        case AllocateCalendarTime(calendarId, timeAllocationManagerId, _, _) if containsTimeAllocationManager(timeAllocationManagerId) =>
          error(s"CalendarTimeManager $calendarId has allocation by TimeallocationManager $timeAllocationManagerId")

        case AllocateCalendarTime(calendarId, timeAllocationManagerId, organizerId, interval) if allocationsTimeLine.hasIntersects(interval.start, interval.end) =>
          CalendarTimeAllocationQueued(calendarId, timeAllocationManagerId, organizerId, interval)

        case AllocateCalendarTime(calendarId, timeAllocationManagerId, organizerId, interval) =>
          CalendarTimeAllocated(calendarId, timeAllocationManagerId, organizerId, interval)

        case DeallocateCalendarTime(calendarId, timeAllocationManagerId) =>

          def processTimeLine: Option[List[CalendarTimeManagerEvent]] =
            allocationsTimeLine
              .neighbours(_.timeAllocationManagerId == timeAllocationManagerId)
              .map{
                case (previous, next) =>
                  AllocationsTimeLine()
                    .canBeAllocated(view(previous.map(_.end), next.map(_.start), allocationTimeQueue))
                      .timeline.map(allocatedTimeInterval =>
                      CalendarTimeAllocatedFromQueue(calendarId, allocatedTimeInterval.timeAllocationManagerId, allocatedTimeInterval.organizerId, Interval(allocatedTimeInterval.start, allocatedTimeInterval.end))
                    )
              }
              .map(CalendarTimeDeallocated(calendarId, timeAllocationManagerId) :: _)

          def processQueue: Option[List[CalendarTimeDeallocatedFromQueue]] =
            allocationTimeQueue
              .find(_.timeAllocationManagerId == timeAllocationManagerId)
              .map(p => List(CalendarTimeDeallocatedFromQueue(calendarId, p.timeAllocationManagerId)))

//          println(s"processTimeLine: $processTimeLine")
//          println(s"processQueue: $processQueue")

          processTimeLine orElse processQueue getOrElse error(s"CalendarTimeManager $calendarId has no allocation by TimeallocationManager $timeAllocationManagerId")
////          def r = List(DeallocateCalendarTimeErrorOccurred(calendarId, s"CalendarTimeManager $calendarId has no allocation by TimeallocationManager $timeAllocationManagerId"))
////
////          def r0 = processTimeLine.orElse(processQueue).getOrElse(r)
////
//          println(s"result: $r0")
//
//          r0
//

        case ReallocateCalendarTime(calendarId, timeAllocationManagerId, organizerId, interval) =>

          def processTimeLine:  Option[List[CalendarTimeManagerEvent]] = {
            allocationsTimeLine
              .neighbours(_.timeAllocationManagerId == timeAllocationManagerId)
              .map{
                case (previous, next) =>
                  val reallocatedIntervals = allocationsTimeLine.remove(_.timeAllocationManagerId == timeAllocationManagerId).canBeAllocated(AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId))

                  val reallocatedFromQueueEvents = reallocatedIntervals.canBeAllocated(view(previous.map(_.end), next.map(_.start), allocationTimeQueue))
                      .timeline.map{allocation =>
                    CalendarTimeAllocatedFromQueue(calendarId, allocation.timeAllocationManagerId, allocation.organizerId, Interval(allocation.start, allocation.end))
                  }

                  if (reallocatedIntervals.isEmpty) {
                    CalendarTimeReallocationQueued(calendarId, timeAllocationManagerId, organizerId, interval) :: reallocatedFromQueueEvents
                  } else {
                    CalendarTimeReallocated(calendarId, timeAllocationManagerId, organizerId, interval) :: reallocatedFromQueueEvents
                  }

              }
          }

          def processQueue:  Option[List[CalendarTimeManagerEvent]] = {
            allocationTimeQueue
              .find(_.timeAllocationManagerId == timeAllocationManagerId)
              .map{allocation =>
                if (allocationsTimeLine.hasIntersects(interval.start, interval.end)) {
                  List(CalendarTimeReallocatedInQueue(calendarId, allocation.timeAllocationManagerId, allocation.organizerId, interval))
                } else {
                  List(CalendarTimeReallocatedFromQueue(calendarId, allocation.timeAllocationManagerId, allocation.organizerId, interval))
                }
              }
          }

          processTimeLine orElse processQueue getOrElse error(s"CalendarTimeManager $calendarId has no allocation by TimeallocationManager $timeAllocationManagerId")

      }
        .handleEvents{
          case CalendarTimeAllocated(_, timeAllocationManagerId, organizerId, interval) =>
            copy(
              allocationsTimeLine = allocationsTimeLine add AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId)
            )
          case CalendarTimeAllocationQueued(_, timeAllocationManagerId, organizerId, interval) =>
            copy(
              allocationTimeQueue = AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId) :: allocationTimeQueue
            )
          case CalendarTimeAllocatedFromQueue(_, timeAllocationManagerId, organizerId, interval) =>
            copy(
              allocationsTimeLine = allocationsTimeLine add AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId),
              allocationTimeQueue = allocationTimeQueue.filterNot(_.timeAllocationManagerId == timeAllocationManagerId)
            )
          case CalendarTimeDeallocated(_, timeAllocationManagerId) =>
            copy(
              allocationsTimeLine = allocationsTimeLine.remove(_.timeAllocationManagerId == timeAllocationManagerId)
            )
          case CalendarTimeDeallocatedFromQueue(_, timeAllocationManagerId) =>
            copy(
              allocationTimeQueue = allocationTimeQueue.filterNot(_.timeAllocationManagerId == timeAllocationManagerId)
            )
          case CalendarTimeReallocated(_, timeAllocationManagerId, organizerId, interval) =>
            copy(
              allocationsTimeLine = allocationsTimeLine.remove(_.timeAllocationManagerId == timeAllocationManagerId) add AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId)
            )
          case CalendarTimeReallocationQueued(_, timeAllocationManagerId, organizerId, interval) =>
            copy(
              allocationsTimeLine = allocationsTimeLine.remove(_.timeAllocationManagerId == timeAllocationManagerId),
              allocationTimeQueue = AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId) :: allocationTimeQueue
            )
          case CalendarTimeReallocatedFromQueue(_, timeAllocationManagerId, organizerId, interval) =>
            copy(
              allocationsTimeLine = allocationsTimeLine add AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId),
              allocationTimeQueue = allocationTimeQueue.filterNot(_.timeAllocationManagerId == timeAllocationManagerId)
            )
          case CalendarTimeReallocatedInQueue(_, timeAllocationManagerId, organizerId, interval) =>
            copy(
              allocationTimeQueue = AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId) :: allocationTimeQueue.filterNot(_.timeAllocationManagerId == timeAllocationManagerId)
            )

//          case _ :CalendarTimeManagerCondition =>
//            this
        }

    }

    override def actions: Actions = allocateActions

  }

}

abstract class CalendarTimeManager(val pc: PassivationConfig) extends AggregateRoot[CalendarTimeManagerEvent, CalendarTimeManagerActions, CalendarTimeManager] {
  this: EventPublisher =>

}
