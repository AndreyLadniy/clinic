package ecommerce.scheduling

import java.time.OffsetDateTime

import akka.actor.Props
import ecommerce.scheduling.timeline.AllocatedTimeInterval
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate.{AggregateRootActorFactory, EntityId}
import pl.newicom.dddd.aggregate.error.{AggregateRootNotInitializedException, DomainException}
import pl.newicom.dddd.eventhandling.LocalPublisher
import pl.newicom.dddd.office.Office
import pl.newicom.dddd.test.support.OfficeSpec

import scala.concurrent.duration._
import scala.concurrent.duration.Duration

object CalendarTimeManagerSpec {
  implicit def factory(implicit it: Duration = 1.minute): AggregateRootActorFactory[CalendarTimeManager] =
    new AggregateRootActorFactory[CalendarTimeManager] {
      override def props(pc: PassivationConfig): Props = Props(new CalendarTimeManager(pc) with LocalPublisher)
      override def inactivityTimeout: Duration = it
    }
}

import CalendarTimeManagerSpec._

class CalendarTimeManagerSpec extends OfficeSpec[CalendarTimeManager] {

  val allocatedTimeInterval_1 = AllocatedTimeInterval(OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z"), "1", "1")
  val allocatedTimeInterval_2 = AllocatedTimeInterval(OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z"), "2", "1")
  val allocatedTimeInterval_3 = AllocatedTimeInterval(OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z"), "3", "1")
  val allocatedTimeInterval_4 = AllocatedTimeInterval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:00Z"), "4", "1")


  def reservationOffice: Office = officeUnderTest

  def calendarId: EntityId = aggregateId

  def timeAllocationManagerId(id: String): String = s"timeAllocationManagerId-$id"

  "CalendarTimeManager office" should {

    "process AllocateCalendarTime command and allocate time" in {

      //add to empty
      when(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
      .expectEvent[CalendarTimeManagerEvent](
        CalendarTimeAllocated(calendarId, timeAllocationManagerId("1"), "1", Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )

      //add after
      given(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z"))),
        AllocateCalendarTime(calendarId, timeAllocationManagerId("2"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
      .when(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("3"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:00Z")))
      )
      .expectEvent[CalendarTimeManagerEvent](
        CalendarTimeAllocated(calendarId, timeAllocationManagerId("3"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:00Z")))
      )

      //add before
      given(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z"))),
        AllocateCalendarTime(calendarId, timeAllocationManagerId("2"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
      .when(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("3"), "1",  Interval(OffsetDateTime.parse("2017-02-13T09:30Z"), OffsetDateTime.parse("2017-02-13T10:00Z")))
      )
      .expectEvent[CalendarTimeManagerEvent](
        CalendarTimeAllocated(calendarId, timeAllocationManagerId("3"), "1",  Interval(OffsetDateTime.parse("2017-02-13T09:30Z"), OffsetDateTime.parse("2017-02-13T10:00Z")))
      )

      //add between
      given(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z"))),
        AllocateCalendarTime(calendarId, timeAllocationManagerId("2"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
      .when(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("3"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:00Z")))
      )
      .expectEvent[CalendarTimeManagerEvent](
        CalendarTimeAllocated(calendarId, timeAllocationManagerId("3"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:00Z")))
      )

    }

    "process AllocateCalendarTime command and add into queue if there is no time for allocation" in {
      given(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
      .when(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("2"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
      .expectEvent[CalendarTimeManagerEvent](
        CalendarTimeAllocationQueued(calendarId, timeAllocationManagerId("2"), "1", Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
    }

    "process AllocateCalendarTime command and throw exception if try allocate twice" in {
      given(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
      .when(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
      .expectException[DomainException](s"CalendarTimeManager $calendarId has allocation by TimeallocationManager ${timeAllocationManagerId("1")}")

      given(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z"))),
        AllocateCalendarTime(calendarId, timeAllocationManagerId("2"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
      .when(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("2"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
      .expectException[DomainException](s"CalendarTimeManager $calendarId has allocation by TimeallocationManager ${timeAllocationManagerId("2")}")

    }

  }

//  "CalendarTimeManager office" should {
//    "process AllocateCalendarTime command and throw exception if try allocate twice" in {
//      given(
//        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
//      )
//        .when(
//          AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
//        )
//        .expectException[RuntimeException](s"CalendarTimeManager $calendarId has allocation by TimeallocationManager ${timeAllocationManagerId("1")}")
//    }
//  }

//  "CalendarTimeManager office" should {
//    "process AllocateCalendarTime command and add into queue if there is no time for allocation" in {
//      given(
//        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z"))),
//        AllocateCalendarTime(calendarId, timeAllocationManagerId("2"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:30Z"))),
//        AllocateCalendarTime(calendarId, timeAllocationManagerId("3"), "1",  Interval(OffsetDateTime.parse("2017-02-13T12:30Z"), OffsetDateTime.parse("2017-02-13T13:30Z")))
//      )
//      .when(
//        AllocateCalendarTime(calendarId, timeAllocationManagerId("4"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))
//      )
//      .expectEvent[CalendarTimeManagerEvent](
//        CalendarTimeAllocationQueued(calendarId, timeAllocationManagerId("4"), "1", Interval(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))
//      )
//    }
//  }


  "CalendarTimeManager office" should {
    "process DeallocateCalendarTime command and throw exception on uninitialized aggregate root" in {
      when(
        DeallocateCalendarTime(calendarId, timeAllocationManagerId("1"))
      )
        .expectException[AggregateRootNotInitializedException]()
    }
  }

  "CalendarTimeManager office" should {
    "process DeallocateCalendarTime command and throw exception if there is no allocations" in {
      given(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("2"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
        .when(
        DeallocateCalendarTime(calendarId, timeAllocationManagerId("1"))
      )
      .expectException[RuntimeException](s"CalendarTimeManager $calendarId has no allocation by TimeallocationManager ${timeAllocationManagerId("1")}")
    }
  }

  "CalendarTimeManager office" should {
    "process DeallocateCalendarTime command and deallocate allocation" in {
      given(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z")))
      )
        .when(
          DeallocateCalendarTime(calendarId, timeAllocationManagerId("1"))
        )
        .expectEvent[CalendarTimeManagerEvent](
          CalendarTimeDeallocated(calendarId, timeAllocationManagerId("1"))
        )
    }
  }

  "CalendarTimeManager office" should {
    "process DeallocateCalendarTime command and deallocate allocation and allocate matched allocations from queue" in {

      given(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z"))),
        AllocateCalendarTime(calendarId, timeAllocationManagerId("2"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:30Z"))),
        AllocateCalendarTime(calendarId, timeAllocationManagerId("3"), "1",  Interval(OffsetDateTime.parse("2017-02-13T12:30Z"), OffsetDateTime.parse("2017-02-13T13:30Z"))),
        AllocateCalendarTime(calendarId, timeAllocationManagerId("4"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))
      )
        .when(
          DeallocateCalendarTime(calendarId, timeAllocationManagerId("2"))
        )
        .expectEvents[CalendarTimeManagerEvent](
          CalendarTimeDeallocated(calendarId, timeAllocationManagerId("2")),
          CalendarTimeAllocatedFromQueue(calendarId, timeAllocationManagerId("4"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))
        )
    }
  }

  "CalendarTimeManager office" should {
    "process DeallocateCalendarTime command and deallocate allocation from queue" in {
      given(
        AllocateCalendarTime(calendarId, timeAllocationManagerId("1"), "1",  Interval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:30Z"))),
        AllocateCalendarTime(calendarId, timeAllocationManagerId("2"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:30Z"))),
        AllocateCalendarTime(calendarId, timeAllocationManagerId("3"), "1",  Interval(OffsetDateTime.parse("2017-02-13T12:30Z"), OffsetDateTime.parse("2017-02-13T13:30Z"))),
        AllocateCalendarTime(calendarId, timeAllocationManagerId("4"), "1",  Interval(OffsetDateTime.parse("2017-02-13T11:30Z"), OffsetDateTime.parse("2017-02-13T12:30Z")))
      )
        .when(
          DeallocateCalendarTime(calendarId, timeAllocationManagerId("4"))
        )
        .expectEvent[CalendarTimeManagerEvent](
          CalendarTimeDeallocatedFromQueue(calendarId, timeAllocationManagerId("4"))
        )
    }
  }


}
