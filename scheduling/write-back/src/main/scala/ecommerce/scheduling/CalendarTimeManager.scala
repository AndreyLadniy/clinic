package ecommerce.scheduling

import java.time.ZonedDateTime

import akka.cluster.sharding.ShardRegion.EntityId
import ecommerce.scheduling.CalendarTimeManager.{Allocation, CalendarTimeManagerActions}
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate._
import pl.newicom.dddd.eventhandling.EventPublisher
import pl.newicom.dddd.office.LocalOfficeId
import pl.newicom.dddd.office.LocalOfficeId.fromRemoteId

import scala.annotation.tailrec
import scala.collection.{AbstractIterable, AbstractIterator}

trait TimeInterval {

  def start: ZonedDateTime

  def end: ZonedDateTime

  def hasIntersect(otherStart: ZonedDateTime, otherEnd: ZonedDateTime): Boolean = start.isBefore(otherEnd) && end.isAfter(otherStart)

}

//case class FreeTimeInterval(start: Option[ZonedDateTime], end: Option[ZonedDateTime]) extends TimeInterval

case class AllocatedTimeInterval(start: ZonedDateTime, end: ZonedDateTime, timeAllocationManagerId: EntityId, organizerId: EntityId) extends TimeInterval {

  def hasIntersect(other: AllocatedTimeInterval): Boolean = hasIntersect(other.start, other.end)

}

trait TimeLine

class AllocationsTimeLine private(allocations: List[AllocatedTimeInterval]) {

  def exists(p: AllocatedTimeInterval => Boolean): Boolean = allocations.exists(p)

  def find(p: AllocatedTimeInterval => Boolean): Option[AllocatedTimeInterval] = allocations.find(p)

  def +=(allocation: AllocatedTimeInterval) : AllocationsTimeLine = {

    val (left, right) = allocations.span(!_.start.isBefore(allocation.end))

//    if (right.nonEmpty) require(!right.head.end.isBefore(allocation.start), "Can not add allocation because interval has intersect with allocated time")

    AllocationsTimeLine(left ::: allocation :: right)
  }

//  def iterator: Iterator[TimeInterval] = {
//    if (allocations.isEmpty) {
//      Iterator.single(FreeTimeInterval(None, None))
//    } else {
//      new AbstractIterator[TimeInterval] {
//        override def hasNext: Boolean = ???
//
//        override def next(): TimeInterval = ???
//      }
//    }
//  }
//
//  def find[A <: TimeInterval](p: A => Boolean): A = {
//
//    var next: Option[ZonedDateTime] = None
//
//    var these = allocations
//
//    while (these.nonEmpty && (!these.head.start.isBefore(time))) {
//      next = Some(these.head.start)
//      these = these.tail
//    }
//
//    val previous = these.headOption.map(_.end)
//
//    (previous, next)
//
//  }


}

object AllocationsTimeLine {

  def apply(): AllocationsTimeLine = new AllocationsTimeLine(Nil)

  def apply(allocations: AllocatedTimeInterval*): AllocationsTimeLine = allocations.foldLeft(AllocationsTimeLine())(_ += _)

  def apply(allocations: List[AllocatedTimeInterval]): AllocationsTimeLine = allocations.foldLeft(AllocationsTimeLine())(_ += _)

}

case class CalendarTimeAllocationsManager(allocations: List[Allocation]) {

  def exists(p: Allocation => Boolean): Boolean = allocations.exists(p)

  def nearest(time: ZonedDateTime): (Option[ZonedDateTime], Option[ZonedDateTime]) = {

    var next: Option[ZonedDateTime] = None

    var these = allocations

    while (these.nonEmpty && (!these.head.start.isBefore(time))) {
      next = Some(these.head.start)
      these = these.tail
    }

    val previous = these.headOption.map(_.end)

    (previous, next)

  }

  def isEmpty(start: ZonedDateTime, end: ZonedDateTime): Boolean = {

    require(end.isAfter(start), "end is not after start")

    nearest(start)._2.forall(!_.isBefore(end))

  }

  def add(allocation: Allocation): CalendarTimeAllocationsManager = {
    val (left, right) = allocations.span(!_.start.isBefore(allocation.end))

    CalendarTimeAllocationsManager(left ::: allocation :: right)
  }

  def remove(p: Allocation => Boolean): CalendarTimeAllocationsManager = {
    CalendarTimeAllocationsManager(allocations.filterNot(p))
  }

  def findWithClosest(p: Allocation => Boolean): (Option[Allocation], Option[Allocation], Option[Allocation]) = {

    var next: Option[Allocation] = None

    var these = allocations

    while (these.nonEmpty && p(these.head)) {
      next = Some(these.head)
      these = these.tail
    }

    val previous = if (these.isEmpty) None else these.tail.headOption


    (if (these.isEmpty) None else these.tail.headOption, these.headOption, next)

  }

  def break(p: Allocation => Boolean) = {

  }

}

//object CalendarTimeAllocationsManager {
//
//  def apply(allocations: List[Allocation]): CalendarTimeAllocationsManager = allocations.foldLeft(new CalendarTimeAllocationsManager(Nil)){case (acc, el) => acc.add(el)}
//
//}

case class CalendarTimeAllocationsQueueManager(queue: List[Allocation]) {

}

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
        case CalendarTimeAllocated(calendarId, timeAllocationManagerId, organizerId, interval) =>
          Created(AllocationsTimeLine(), CalendarTimeAllocationsManager(List(Allocation(timeAllocationManagerId, organizerId, interval.start, interval.end))), List(Allocation(timeAllocationManagerId, organizerId, interval.start, interval.end)), List.empty)
      }
  }

  def allocate(start: Option[ZonedDateTime], end: Option[ZonedDateTime], queuedAllocations: List[Allocation]): List[Allocation] = {
    queuedAllocations
      .filter(allocation => start.fold(true)(!_.isAfter(allocation.start)) && end.fold(true)(!_.isBefore(allocation.end)))
      .foldLeft(List.empty[Allocation]){case (acc, el) =>
          acc.find(_.hasIntersect(el)) match {
            case Some(x) => acc
            case None => el :: acc
          }
      }
  }

  def addAllocation(allocation:Allocation, allocations: List[Allocation]): List[Allocation] = {
    val (left, right) = allocations.span(!_.start.isBefore(allocation.end))

    left ::: allocation :: right
  }

  def spanAllocatedTime(allocation: Allocation, allocations: List[Allocation]): Option[(List[Allocation], List[Allocation])] = {
    if (allocations.isEmpty) {
      Some(Nil, Nil)
    } else {

      val (left, right) = allocations.span(!_.start.isBefore(allocation.end))

      if (right.isEmpty || !right.head.end.isAfter(allocation.start)) {
        Some(left, right)
      } else {
        None
      }
    }
  }

  def findAllocatedTime(timeAllocationManagerId: EntityId, allocatedTime: List[Allocation]): (Option[Allocation], Option[Allocation], Option[Allocation]) = {

    var after: Option[Allocation] = None

    var these = allocatedTime

    while (these.nonEmpty && these.head.timeAllocationManagerId != timeAllocationManagerId) {
      after = Some(these.head)
      these = these.tail
    }

    (after, these.headOption, if (these.isEmpty) None else these.tail.headOption)

  }

  /*
  * allocatedTime is the ordered list of allocations in reverse order last -> first
  * */
  case class Created(allocationsTimeLine: AllocationsTimeLine, allocationTimeManager: CalendarTimeAllocationsManager, allocatedTime: List[Allocation], allocationTimeQueue: List[Allocation]) extends CalendarTimeManagerActions {

    def isFreeForAllocation(start: ZonedDateTime, end: ZonedDateTime): Boolean = {
      if (allocatedTime.isEmpty) {
        true
      } else {

        var these = allocatedTime

        while (these.nonEmpty && !these.head.start.isBefore(end)) {
          these = these.tail
        }

        if (these.isEmpty) {
          true
        } else {
          !these.head.end.isAfter(start)
        }
      }
    }

    def actions: Actions =
      handleCommands{
        case AllocateCalendarTime(calendarId, timeAllocationManagerId, organizerId, interval) =>
          def timeAllocationManagerIdСonstraint(p: Allocation): Boolean = p.timeAllocationManagerId == timeAllocationManagerId

          require(!allocationTimeManager.exists(timeAllocationManagerIdСonstraint) && !allocationTimeQueue.exists(timeAllocationManagerIdСonstraint), s"CalendarTimeManager $calendarId has allocation by TimeallocationManager $timeAllocationManagerId")

          allocationsTimeLine.find(_.hasIntersect(interval.start, interval.end)) match {
            case Some(x) => CalendarTimeAllocationQueued(calendarId, timeAllocationManagerId, organizerId, interval)
            case None => CalendarTimeAllocated(calendarId, timeAllocationManagerId, organizerId, interval)
          }

//          if (allocationTimeManager.isEmpty(interval.start, interval.end)) {
//            CalendarTimeAllocated(calendarId, timeAllocationManagerId, organizerId, interval)
//          } else {
//            CalendarTimeAllocationQueued(calendarId, timeAllocationManagerId, organizerId, interval)
//          }

//          if (allocatedTime.exists(_.timeAllocationManagerId == timeAllocationManagerId) || allocationTimeQueue.exists(_.timeAllocationManagerId == timeAllocationManagerId)) {
//            throw new RuntimeException(s"CalendarTimeManager $calendarId has allocation by TimeallocationManager $timeAllocationManagerId")
//          } else {
//            if (isFreeForAllocation(interval.start, interval.end)) {
////              if (allocatedTime.exists(_.hasIntersect(interval))) {
//              CalendarTimeAllocated(calendarId, timeAllocationManagerId, organizerId, interval)
//            } else {
//              CalendarTimeAllocationQueued(calendarId, timeAllocationManagerId, organizerId, interval)
//            }
//          }
        case DeallocateCalendarTime(calendarId, timeAllocationManagerId) =>

//          val (allocationAfter, allocation, allocationBefore) = findAllocatedTime(timeAllocationManagerId, allocatedTime)

          val (allocationBefore, allocation, allocationAfter) = allocationTimeManager.findWithClosest(_.timeAllocationManagerId != timeAllocationManagerId)

          val queuedAllocation = allocationTimeQueue.find(_.timeAllocationManagerId == timeAllocationManagerId)

          if (allocation.isEmpty && queuedAllocation.isEmpty) {
            throw new RuntimeException(s"CalendarTimeManager $calendarId has no allocation by TimeallocationManager $timeAllocationManagerId")
          } else {

            val allocationEvents: List[CalendarTimeManagerEvent] = allocation.map{ p =>
              val allocatedFromQueue = allocate(allocationBefore.map(_.end), allocationAfter.map(_.start), allocationTimeQueue)
                .map(allocationFromQueue => CalendarTimeAllocatedFromQueue(calendarId, allocationFromQueue.timeAllocationManagerId, allocationFromQueue.organizerId, Interval(allocationFromQueue.start, allocationFromQueue.end)))

              CalendarTimeDeallocated(calendarId, p.timeAllocationManagerId) :: allocatedFromQueue
            }.getOrElse(Nil)

            val queuedAllocationEvents: List[CalendarTimeManagerEvent] = queuedAllocation.map { p =>
              List(CalendarTimeDeallocatedFromQueue(calendarId, p.timeAllocationManagerId))
            }.getOrElse(Nil)

            allocationEvents ::: queuedAllocationEvents

          }
        case command @ ReallocateCalendarTime(calendarId, timeAllocationManagerId, organizerId, interval) =>

          val (allocationAfter, allocation, allocationBefore) = findAllocatedTime(timeAllocationManagerId, allocatedTime)

          val queuedAllocation = allocationTimeQueue.find(_.timeAllocationManagerId == timeAllocationManagerId)

          if (allocation.isEmpty && queuedAllocation.isEmpty) {
            throw new RuntimeException(s"CalendarTimeManager $calendarId has no allocation by TimeallocationManager $timeAllocationManagerId")
          } else {

            def reallocateFromOrInQueue(p: Allocation): CalendarTimeManagerEvent = {
              //try allocate this allocation or update in queue in other case
              if (allocatedTime.exists(_.hasIntersect(p))) {
                CalendarTimeReallocatedInQueue(command)
              } else {
                CalendarTimeReallocatedFromQueue(command)
              }
            }

            def reallocateInAllocationTime(p: Allocation) = {

            }

//            queuedAllocation.map(reallocateFromOrInQueue) orElse ()

//            val allocationEvents: List[CalendarTimeManagerEvent] = allocation.map{ p =>

            val (deallocatedCalendarTime, resultAllocatedTime) = allocatedTime.partition(p => p.timeAllocationManagerId == timeAllocationManagerId)

            if (resultAllocatedTime.exists(p => p.hasIntersect(interval) && (p.timeAllocationManagerId != timeAllocationManagerId))) {
              CalendarTimeReallocationQueued(calendarId, timeAllocationManagerId, organizerId, interval)
            } else {
              CalendarTimeReallocated(calendarId, timeAllocationManagerId, organizerId, interval)
            }
          }
      }
      .handleEvents{
        case event @ CalendarTimeAllocated(_, timeAllocationManagerId, organizerId, interval) =>
          copy(
            allocationsTimeLine = allocationsTimeLine += AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId),
            allocationTimeManager = allocationTimeManager.add(Allocation(timeAllocationManagerId, organizerId, interval.start, interval.end)),
            allocatedTime = addAllocation(Allocation(timeAllocationManagerId, organizerId, interval.start, interval.end), allocatedTime)
          )
        case CalendarTimeAllocationQueued(_, timeAllocationManagerId, organizerId, interval) =>
          copy(
            allocationTimeQueue = Allocation(timeAllocationManagerId, organizerId, interval.start, interval.end) :: allocationTimeQueue
          )
        case CalendarTimeAllocatedFromQueue(_, timeAllocationManagerId, organizerId, interval) =>
          copy(
            allocationsTimeLine = allocationsTimeLine += AllocatedTimeInterval(interval.start, interval.end, timeAllocationManagerId, organizerId),
            allocationTimeManager = allocationTimeManager.add(Allocation(timeAllocationManagerId, organizerId, interval.start, interval.end)),
            allocatedTime = addAllocation(Allocation(timeAllocationManagerId, organizerId, interval.start, interval.end), allocatedTime),
            allocationTimeQueue = allocationTimeQueue.filterNot(_.timeAllocationManagerId == timeAllocationManagerId)
          )
        case CalendarTimeDeallocated(_, timeAllocationManagerId) =>
          copy(
            allocationTimeManager = allocationTimeManager.remove(_.timeAllocationManagerId == timeAllocationManagerId),
            allocatedTime = allocatedTime.filterNot(_.timeAllocationManagerId == timeAllocationManagerId)
          )
        case CalendarTimeDeallocatedFromQueue(_, timeAllocationManagerId) =>
          copy(
            allocationTimeQueue = allocationTimeQueue.filterNot(_.timeAllocationManagerId == timeAllocationManagerId)
          )
//        Reallocation
        case CalendarTimeReallocated(_, calendarTimeAllocationId, organizerId, interval) =>
          copy(
            allocatedTime = addAllocation(Allocation(calendarTimeAllocationId, organizerId, interval.start, interval.end), allocatedTime.filterNot(_.timeAllocationManagerId == calendarTimeAllocationId))
          )
        case CalendarTimeReallocationQueued(_, calendarTimeAllocationId, organizerId, interval) =>
          copy(
            allocatedTime = allocatedTime.filterNot(_.timeAllocationManagerId == calendarTimeAllocationId),
            allocationTimeQueue = Allocation(calendarTimeAllocationId, organizerId, interval.start, interval.end) :: allocationTimeQueue
          )
        case CalendarTimeReallocatedFromQueue(_, calendarTimeAllocationId, organizerId, interval) =>
          copy(
            allocatedTime = addAllocation(Allocation(calendarTimeAllocationId, organizerId, interval.start, interval.end), allocatedTime.filterNot(_.timeAllocationManagerId == calendarTimeAllocationId)),
            allocationTimeQueue = allocationTimeQueue.filterNot(_.timeAllocationManagerId == calendarTimeAllocationId)
          )
        case CalendarTimeReallocatedInQueue(_, calendarTimeAllocationId, organizerId, interval) =>
          copy(
            allocationTimeQueue = Allocation(calendarTimeAllocationId, organizerId, interval.start, interval.end) :: allocationTimeQueue.filterNot(_.timeAllocationManagerId == calendarTimeAllocationId)
          )
      }
  }

  case class Allocation(timeAllocationManagerId: EntityId, organizerId: EntityId, start: ZonedDateTime, end: ZonedDateTime) {

     def hasIntersect(otherStart: ZonedDateTime, otherEnd: ZonedDateTime): Boolean = start.isBefore(otherEnd) && otherStart.isBefore(end)

     def hasIntersect(other: Interval): Boolean = hasIntersect(other.start, other.end)

      def hasIntersect(other: Allocation): Boolean = hasIntersect(other.start, other.end)

  }

}

abstract class CalendarTimeManager(val pc: PassivationConfig) extends AggregateRoot[CalendarTimeManagerEvent, CalendarTimeManagerActions, CalendarTimeManager] {
  this: EventPublisher =>

}
