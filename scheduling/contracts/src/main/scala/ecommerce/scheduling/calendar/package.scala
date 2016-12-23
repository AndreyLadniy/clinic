package ecommerce.scheduling

import pl.newicom.dddd.office.RemoteOfficeId

package object calendar {

  implicit object CalendarOfficeId extends RemoteOfficeId[ecommerce.scheduling.calendar.CalendarCommand]("Calendar", "Scheduling", classOf[ecommerce.scheduling.calendar.CalendarCommand])

}
