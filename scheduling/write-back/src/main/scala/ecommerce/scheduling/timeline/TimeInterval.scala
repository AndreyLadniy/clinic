package ecommerce.scheduling.timeline

import java.time.OffsetDateTime

import pl.newicom.dddd.aggregate.EntityId

sealed trait TimeInterval {

  def start: OffsetDateTime

  def end: OffsetDateTime

  def hasIntersect(otherStart: OffsetDateTime, otherEnd: OffsetDateTime): Boolean = start.isBefore(otherEnd) && end.isAfter(otherStart)

  def hasIntersect(timeInterval: TimeInterval): Boolean = hasIntersect(timeInterval.start, timeInterval.end)

}

case class SimpleInterval(start: OffsetDateTime, end: OffsetDateTime) extends TimeInterval

case class AllocatedTimeInterval(start: OffsetDateTime, end: OffsetDateTime, timeAllocationManagerId: EntityId, organizerId: EntityId) extends TimeInterval {

//  def hasIntersect(other: AllocatedTimeInterval): Boolean = hasIntersect(other.start, other.end)

}

case class QueuedAllocationTimeInterval(start: OffsetDateTime, end: OffsetDateTime, timeAllocationManagerId: EntityId, organizerId: EntityId) extends TimeInterval {

  //  def hasIntersect(other: AllocatedTimeInterval): Boolean = hasIntersect(other.start, other.end)

}

case class BusyEventTimeInterval(start: OffsetDateTime, end: OffsetDateTime) extends TimeInterval