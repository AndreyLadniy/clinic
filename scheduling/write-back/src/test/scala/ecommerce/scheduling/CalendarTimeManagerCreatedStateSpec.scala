package ecommerce.scheduling

import java.time.ZonedDateTime

import ecommerce.scheduling.CalendarTimeManager.{Allocation, CalendarTimeManagerActions, Created}
import org.scalatest.{GivenWhenThen, WordSpec, WordSpecLike}

class CalendarTimeManagerCreatedStateSpec extends WordSpec with GivenWhenThen{

  "CalendarTimeManager state" when {
    "is Created" must {
      "change state on CalendarTimeAllocated event" in {

      Given("empty Created state")
      var state: CalendarTimeManagerActions = Created(CalendarTimeAllocationsManager(Nil), Nil, Nil)

      When("first CalendarTimeAllocated event is received")
      state = state.apply(CalendarTimeAllocated("1", "1", "1", Interval(ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z"))))

      Then("state must have first allocation")
      assert(
        state == Created(
          CalendarTimeAllocationsManager(Nil),
          List(
            Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z"))
          ),
          Nil
        )
      )

      When("greater CalendarTimeAllocated event is received")
      state = state.apply(CalendarTimeAllocated("1", "2", "1", Interval(ZonedDateTime.parse("2017-02-13T12:00Z"), ZonedDateTime.parse("2017-02-13T12:30Z"))))

      Then("state must have prepended allocation")
      assert(
        state == Created(
          CalendarTimeAllocationsManager(Nil),
          List(
            Allocation("2", "1", ZonedDateTime.parse("2017-02-13T12:00Z"), ZonedDateTime.parse("2017-02-13T12:30Z")),
            Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z"))
          ),
          Nil
        )
      )

      When("lower CalendarTimeAllocated event is received")
      state = state.apply(CalendarTimeAllocated("1", "3", "1", Interval(ZonedDateTime.parse("2017-02-13T10:00Z"), ZonedDateTime.parse("2017-02-13T10:30Z"))))

      Then("state must have appended allocation")
      assert(
        state == Created(
          CalendarTimeAllocationsManager(Nil),
          List(
            Allocation("2", "1", ZonedDateTime.parse("2017-02-13T12:00Z"), ZonedDateTime.parse("2017-02-13T12:30Z")),
            Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z")),
            Allocation("3", "1", ZonedDateTime.parse("2017-02-13T10:00Z"), ZonedDateTime.parse("2017-02-13T10:30Z"))
          ),
          Nil
        )
      )

      When("middle CalendarTimeAllocated event is received")
      state = state.apply(CalendarTimeAllocated("1", "4", "1", Interval(ZonedDateTime.parse("2017-02-13T10:30Z"), ZonedDateTime.parse("2017-02-13T11:00Z"))))

      Then("state must have inserted allocation")
      assert(
        state == Created(
          CalendarTimeAllocationsManager(Nil),
          List(
            Allocation("2", "1", ZonedDateTime.parse("2017-02-13T12:00Z"), ZonedDateTime.parse("2017-02-13T12:30Z")),
            Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z")),
            Allocation("4", "1", ZonedDateTime.parse("2017-02-13T10:30Z"), ZonedDateTime.parse("2017-02-13T11:00Z")),
            Allocation("3", "1", ZonedDateTime.parse("2017-02-13T10:00Z"), ZonedDateTime.parse("2017-02-13T10:30Z"))
          ),
          Nil
        )
      )

    }

      "have addAllocation function" which {

        "add allocation in empty allocations" in {
          val allocations = CalendarTimeManager.addAllocation(Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z")), Nil)

          assert(
            allocations == List(
              Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z"))
            )
          )
        }

        "prepend allocation if later" in {

          val allocations = CalendarTimeManager.addAllocation(
            Allocation("2", "1", ZonedDateTime.parse("2017-02-13T12:00Z"), ZonedDateTime.parse("2017-02-13T12:30Z")),
            List(
              Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z"))
            )
          )

          assert(
            allocations == List(
              Allocation("2", "1", ZonedDateTime.parse("2017-02-13T12:00Z"), ZonedDateTime.parse("2017-02-13T12:30Z")),
              Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z"))
            )
          )
        }

        "append allocation if earlier" in {

          val allocations = CalendarTimeManager.addAllocation(
            Allocation("3", "1", ZonedDateTime.parse("2017-02-13T10:00Z"), ZonedDateTime.parse("2017-02-13T10:30Z")),
            List(
              Allocation("2", "1", ZonedDateTime.parse("2017-02-13T12:00Z"), ZonedDateTime.parse("2017-02-13T12:30Z")),
              Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z"))
            )
          )

          assert(
            allocations == List(
              Allocation("2", "1", ZonedDateTime.parse("2017-02-13T12:00Z"), ZonedDateTime.parse("2017-02-13T12:30Z")),
              Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z")),
              Allocation("3", "1", ZonedDateTime.parse("2017-02-13T10:00Z"), ZonedDateTime.parse("2017-02-13T10:30Z"))
            )
          )
        }

        "insert allocation if between" in {

          val allocations = CalendarTimeManager.addAllocation(
            Allocation("4", "1", ZonedDateTime.parse("2017-02-13T10:30Z"), ZonedDateTime.parse("2017-02-13T11:00Z")),
            List(
              Allocation("2", "1", ZonedDateTime.parse("2017-02-13T12:00Z"), ZonedDateTime.parse("2017-02-13T12:30Z")),
              Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z")),
              Allocation("3", "1", ZonedDateTime.parse("2017-02-13T10:00Z"), ZonedDateTime.parse("2017-02-13T10:30Z"))
            )
          )

          assert(
            allocations == List(
              Allocation("2", "1", ZonedDateTime.parse("2017-02-13T12:00Z"), ZonedDateTime.parse("2017-02-13T12:30Z")),
              Allocation("1", "1", ZonedDateTime.parse("2017-02-13T11:00Z"), ZonedDateTime.parse("2017-02-13T11:30Z")),
              Allocation("4", "1", ZonedDateTime.parse("2017-02-13T10:30Z"), ZonedDateTime.parse("2017-02-13T11:00Z")),
              Allocation("3", "1", ZonedDateTime.parse("2017-02-13T10:00Z"), ZonedDateTime.parse("2017-02-13T10:30Z"))
            )
          )
        }

      }
    }
  }

}


