package ecommerce.scheduling

import java.time.OffsetDateTime

import ecommerce.scheduling.timeline.{AllocatedTimeInterval, AllocationsTimeLine}
import org.scalatest.{Matchers, WordSpec}

class AllocationsTimeLineSpec extends WordSpec with Matchers  {

  val emptyAllocationsTimeLine = AllocationsTimeLine()

  val allocationsTimeLineWithOneInterval = emptyAllocationsTimeLine add AllocatedTimeInterval(OffsetDateTime.parse("2017-02-13T11:00Z"), OffsetDateTime.parse("2017-02-13T11:30Z"), "1", "1")

  val allocationsTimeLineWithTwoIntervals = allocationsTimeLineWithOneInterval add AllocatedTimeInterval(OffsetDateTime.parse("2017-02-13T12:00Z"), OffsetDateTime.parse("2017-02-13T12:30Z"), "2", "1")

  val allocationsTimeLineWithThreeIntervals = allocationsTimeLineWithTwoIntervals add AllocatedTimeInterval(OffsetDateTime.parse("2017-02-13T10:00Z"), OffsetDateTime.parse("2017-02-13T10:30Z"), "3", "1")

  val allocationsTimeLineWithFourIntervals = allocationsTimeLineWithThreeIntervals add AllocatedTimeInterval(OffsetDateTime.parse("2017-02-13T10:30Z"), OffsetDateTime.parse("2017-02-13T11:00Z"), "4", "1")

  "allocation time line" should {
    "provide exists function" which {
      "returns false on empty time line" in {
        emptyAllocationsTimeLine.exists(_.timeAllocationManagerId == "1") shouldEqual false
      }
      "returns false on nonempty time line if there is matched value" in {
        allocationsTimeLineWithOneInterval.exists(_.timeAllocationManagerId == "1") shouldEqual false
      }
    }
  }
}
