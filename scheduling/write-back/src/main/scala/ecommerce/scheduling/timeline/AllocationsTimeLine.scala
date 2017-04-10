package ecommerce.scheduling.timeline

import java.time.OffsetDateTime

import collection.IterableFactories
import pl.newicom.dddd.aggregate.EntityId

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class AllocationsTimeLine private(allocations: List[AllocatedTimeInterval], queue: List[AllocatedTimeInterval]) {
//  extends TimeLine[AllocatedTimeInterval]{

  private final val ordering = Ordering.fromLessThan[AllocatedTimeInterval]{case (x,y) => x.end.isBefore(y.end)}

  //  def fromIterable[B <: AllocatedTimeInterval](c: collection.Iterable[B]): AllocationsTimeLine = AllocationsTimeLine.fromIterable(c)

  def isEmpty: Boolean = allocations.isEmpty

  def map[B](f: AllocatedTimeInterval => B): List[B] = allocations.map(f)

  def exists(p: AllocatedTimeInterval => Boolean): Boolean = allocations.exists(p)

  def hasIntersects(start: OffsetDateTime, end: OffsetDateTime): Boolean = exists(_.hasIntersect(start, end))

  def hasAllocatedTimeIntervalByAllocationTimeManagerId(timeAllocationManagerId: EntityId): Boolean = exists(_.timeAllocationManagerId == timeAllocationManagerId)

  def add(allocation: AllocatedTimeInterval) : AllocationsTimeLine = {

    //    val (left, right) = allocations.span(!_.start.isBefore(allocation.end))
    val (left, right) = allocations.span(ordering.compare(_, allocation) == 1)

    //    if (right.nonEmpty) require(!right.head.end.isBefore(allocation.start), "Can not add allocation because interval has intersect with allocated time")

    new AllocationsTimeLine(left ::: allocation :: right, queue)
  }

//  def addToQueue(allocatedTimeInterval: AllocatedTimeInterval): AllocationsTimeLine = new AllocationsTimeLine(allocations, allocatedTimeInterval :: queue)

  def remove(p: AllocatedTimeInterval => Boolean): AllocationsTimeLine = new AllocationsTimeLine(allocations.filterNot(p), queue)

//  def removeFromQueue(allocatedTimeInterval: AllocatedTimeInterval): AllocationsTimeLine = new AllocationsTimeLine(allocations, queue.filterNot(_ == allocatedTimeInterval))

  @tailrec private def neighbours0(next: Option[AllocatedTimeInterval], l: List[AllocatedTimeInterval], p: AllocatedTimeInterval => Boolean): Option[(Option[AllocatedTimeInterval], Option[AllocatedTimeInterval])] = {
    l match {
      case Nil => None
      case x::xs if p(x) => Some(xs.headOption, next)
      case x::xs => neighbours0(Some(x), xs, p)
    }
  }

  def neighbours(p: AllocatedTimeInterval => Boolean): Option[(Option[AllocatedTimeInterval], Option[AllocatedTimeInterval])] = {
    neighbours0(None, allocations, p)
  }

  def timeline: List[AllocatedTimeInterval] = allocations

  def canBeAllocated(forAllocation: List[AllocatedTimeInterval]): AllocationsTimeLine = {
    forAllocation
      .view
      .filterNot(timeInterval => allocations.exists(_.hasIntersect(timeInterval)))
      .foldLeft(AllocationsTimeLine()){
        case (acc, el) if acc.exists(_.hasIntersect(el.start, el.end)) => acc
        case (acc, el) => acc add el
      }
  }

  def canBeAllocated(forAllocation: AllocatedTimeInterval): AllocationsTimeLine = {
    if (allocations.exists(_.hasIntersect(forAllocation))) {
      AllocationsTimeLine()
    } else {
      AllocationsTimeLine() add forAllocation
    }
  }

  def iterator: Iterator[AllocatedTimeInterval] = allocations.iterator
}

object AllocationsTimeLine {
//  extends IterableFactories[TimeLine] {

  def apply(): AllocationsTimeLine = new AllocationsTimeLine(Nil, Nil)

  def fromIterable[B <: AllocatedTimeInterval](coll: Iterable[B]): AllocationsTimeLine = coll.foldLeft(AllocationsTimeLine()){case (acc, el) => acc add el}

  def newBuilder[A <: AllocatedTimeInterval]: mutable.Builder[A, AllocationsTimeLine] = new ListBuffer[A].mapResult(fromIterable(_))

}

class CalendarTimeLine private(allocations: List[AllocatedTimeInterval], events: List[BusyEventTimeInterval]) {



}

object CalendarTimeLine {



}