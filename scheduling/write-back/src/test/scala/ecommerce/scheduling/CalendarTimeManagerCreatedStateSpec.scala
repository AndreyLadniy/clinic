package ecommerce.scheduling

import java.time.OffsetDateTime

import ecommerce.scheduling.CalendarTimeManager.Active
import ecommerce.scheduling.timeline.{AllocatedTimeInterval, AllocationsTimeLine}
import org.scalatest.{GivenWhenThen, WordSpec}

class CalendarTimeManagerCreatedStateSpec extends WordSpec with GivenWhenThen{

  "CalendarTimeManager action" when {
    "is Created" should {
      "receive AllocateCalendarTime command and produce CalendarTimeAllocated event if AllocationsTimeLine has no intersects with interval from command" in {

        def toCalendarTimeAllocated(actions: CalendarTimeManager.Active, interval: Interval) = actions.handleCommand(AllocateCalendarTime("1", "1", "1", interval)) == Seq( CalendarTimeAllocated("1", "1", "1", interval) )

        object AllocationsTimeLineWithoutIntersections extends AllocationsTimeLine {
          override def exists(p: AllocatedTimeInterval => Boolean): Boolean = false
        }

        val actions: CalendarTimeManager.Active = Active(
          AllocationsTimeLineWithoutIntersections,
          Nil
        )

        assert(toCalendarTimeAllocated(actions, Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:01Z"))))

      }

      "receive AllocateCalendarTime command and produce CalendarTimeAllocationQueued event if AllocationsTimeLine has intersecting with interval from command" in {

        def toCalendarTimeAllocationQueued(actions: CalendarTimeManager.Active, interval: Interval) = actions.handleCommand(AllocateCalendarTime("1", "1", "1", interval)) == Seq( CalendarTimeAllocationQueued("1", "1", "1", interval) )

        object AllocationsTimeLineWithIntersections extends AllocationsTimeLine {
          override def exists(p: AllocatedTimeInterval => Boolean): Boolean = true
        }

        val actions: CalendarTimeManager.Active = Active(
          AllocationsTimeLineWithIntersections,
          Nil
        )

        assert(toCalendarTimeAllocationQueued(actions, Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T12:00Z"))))

      }
    }
  }

}


