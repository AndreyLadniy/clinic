package ecommerce

import pl.newicom.dddd.office.RemoteOfficeId

package object scheduling {

  implicit object CalendarOfficeId extends RemoteOfficeId[CalendarCommand]("Calendar", "Scheduling", classOf[CalendarCommand])

  implicit object CalendarTimeManagerOfficeId extends RemoteOfficeId[CalendarTimeManagerCommand]("CalendarTimeManager", "Scheduling", classOf[CalendarTimeManagerCommand])

  implicit object TimeAllocationManagerOfficeId extends RemoteOfficeId[TimeAllocationManagerCommand]("TimeAllocationManager", "Scheduling", classOf[TimeAllocationManagerCommand])

}
