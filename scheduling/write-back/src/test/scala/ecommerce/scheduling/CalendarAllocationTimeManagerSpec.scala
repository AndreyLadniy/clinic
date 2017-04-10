package ecommerce.scheduling

import java.time.OffsetDateTime

import ecommerce.scheduling.CalendarTimeManager.Allocation
import org.scalatest.{GivenWhenThen, Matchers, WordSpec}

class CalendarAllocationTimeManagerSpec extends WordSpec with Matchers with GivenWhenThen {

  "CalendarAllocationTimeManager" should {
    "provide an 'add' function" in {
      var manager = CalendarTimeAllocationsManager(Nil)

      assert(manager.allocations == Nil)

      manager = manager.add(Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))

      assert(
        manager.allocations == List(
          Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z"))
        )
      )

      manager = manager.add(Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z")))

      assert(
        manager.allocations == List(
          Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z")),
          Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z"))
        )
      )

      manager = manager.add(Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))

      assert(
        manager.allocations == List(
          Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z")),
          Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")),
          Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z"))
        )
      )

      manager = manager.add(Allocation("4", "1", OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:00Z")))

      assert(
        manager.allocations == List(
          Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z")),
          Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")),
          Allocation("4", "1", OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:00Z")),
          Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z"))
        )
      )
    }

    "provide an 'remove' function" in {
      var manager = CalendarTimeAllocationsManager(Nil)
        .add(Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))
        .add(Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z")))
        .add(Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
        .add(Allocation("4", "1", OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:00Z")))

      manager = manager.remove(_.timeAllocationManagerId == "4")

      assert(
        manager.allocations == List(
          Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z")),
          Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")),
          Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z"))
        )
      )

      manager = manager.remove(_.timeAllocationManagerId == "1")

      assert(
        manager.allocations == List(
          Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")),
          Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z"))
        )
      )

      manager = manager.remove(_.timeAllocationManagerId == "2")

      assert(
        manager.allocations == List(
          Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z"))
        )
      )

      manager = manager.remove(_.timeAllocationManagerId == "3")

      assert(manager.allocations == Nil)

    }

    "provide an 'nearest' function" which {
      "returns (None, None) on empty allocation list" in {
        val manager = CalendarTimeAllocationsManager(Nil)

        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T11:30Z")) == (None, None))
      }

      "returns (None, Some(_)) if the time is less than or equal to the earliest start of interval" in {
        val manager = CalendarTimeAllocationsManager(Nil)
          .add(Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))
          .add(Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z")))
          .add(Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))

        println(manager.allocations)

        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T10:00Z")) == (None, Some(OffsetDateTime.parse("2017-02-13T10:00Z"))))

        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T09:30Z")) == (None, Some(OffsetDateTime.parse("2017-02-13T10:00Z"))))
      }

      "returns (Some(_), None) if the time is greater than or equal to the very late end of the interval" in {
        val manager = CalendarTimeAllocationsManager(Nil)
          .add(Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))
          .add(Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z")))
          .add(Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))


        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T12:30Z")) == (Some(OffsetDateTime.parse("2017-02-13T12:30Z")), None))

        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T13:00Z")) == (Some(OffsetDateTime.parse("2017-02-13T12:30Z")), None))
      }

      "returns (Some(_), Some(_)) if the time is between of the intervals" in {
        val manager = CalendarTimeAllocationsManager(Nil)
          .add(Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))
          .add(Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z")))
          .add(Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))


        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T11:30Z")) == (Some(OffsetDateTime.parse("2017-02-13T11:30Z")), Some(OffsetDateTime.parse("2017-02-13T12:00Z"))))
        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T11:40Z")) == (Some(OffsetDateTime.parse("2017-02-13T11:30Z")), Some(OffsetDateTime.parse("2017-02-13T12:00Z"))))
        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T12:00Z")) == (Some(OffsetDateTime.parse("2017-02-13T11:30Z")), Some(OffsetDateTime.parse("2017-02-13T12:00Z"))))

        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T10:30Z")) == (Some(OffsetDateTime.parse("2017-02-13T10:30Z")), Some(OffsetDateTime.parse("2017-02-13T11:00Z"))))
        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T10:40Z")) == (Some(OffsetDateTime.parse("2017-02-13T10:30Z")), Some(OffsetDateTime.parse("2017-02-13T11:00Z"))))
        assert(manager.nearest(OffsetDateTime.parse("2017-02-13T11:00Z")) == (Some(OffsetDateTime.parse("2017-02-13T10:30Z")), Some(OffsetDateTime.parse("2017-02-13T11:00Z"))))
      }

    }

    "provide an 'isEmpty' function" which {

      var manager = CalendarTimeAllocationsManager(Nil)
        .add(Allocation("1", "1", OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))
        .add(Allocation("2", "1", OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z")))
        .add(Allocation("3", "1", OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
        .add(Allocation("4", "1", OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:00Z")))

      "produce IllegalArgumentException when start after end" in {

        an[IllegalArgumentException] should be thrownBy manager.isEmpty(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T11:00Z"))

      }

      "returns true if interval with same start edges is empty" in {
        manager.isEmpty(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T11:50Z")) shouldEqual true
      }
      "returns true if interval with same end edges is empty" in {
        manager.isEmpty(OffsetDateTime.parse("2017-02-13T11:40Z"), OffsetDateTime.parse("2017-02-13T12:00Z")) shouldEqual true
      }
      "returns true if interval with same start and end edges is empty" in {
        manager.isEmpty(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:00Z")) shouldEqual true
      }
      "returns true if interval between edges is empty" in {
        manager.isEmpty(OffsetDateTime.parse("2017-02-13T11:40Z"), OffsetDateTime.parse("2017-02-13T11:50Z")) shouldEqual true
      }

      "returns false if interval between edges is not empty" in {
        manager.isEmpty(OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:01Z")) shouldEqual false

      }

    }

  }
}
