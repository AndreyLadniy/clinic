package ecommerce.scheduling

import java.time.ZonedDateTime


case class Interval(start: ZonedDateTime, end: ZonedDateTime) {
  def hasIntersect(other: Interval): Boolean = start.isBefore(other.end) && other.start.isBefore(end)
}

object Interval {

  trait IntervalOrdering extends Ordering[Interval] {
    def compare(x: Interval, y: Interval): Int =
    //      if (x.start<y.end && y.start < x.end) 0
    //      else if (x.end<=y.start) -1
    //      else 1
      if (x.start.isBefore(y.end) && y.start.isBefore(x.end)) 0
      else if (x.end.isBefore(y.start) || x.end.isEqual(y.start)) -1
      else 1
  }

  implicit object IntervalOrderingObject extends IntervalOrdering

}
