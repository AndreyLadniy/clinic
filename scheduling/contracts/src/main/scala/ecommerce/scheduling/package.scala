package ecommerce

import pl.newicom.dddd.office.RemoteOfficeId

package object scheduling {

  implicit object OrganizerReservationOfficeId extends RemoteOfficeId[scheduling.Command]("OrganizerReservation", "Scheduling", classOf[scheduling.Command])

  implicit object CalendarTimeManagerOfficeId extends RemoteOfficeId[CalendarTimeManagerCommand]("CalendarTimeManager", "Scheduling", classOf[CalendarTimeManagerCommand])

  implicit object TimeAllocationManagerOfficeId extends RemoteOfficeId[TimeAllocationManagerCommand]("TimeAllocationManager", "Scheduling", classOf[TimeAllocationManagerCommand])

}
