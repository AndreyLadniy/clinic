package ecommerce.scheduling.view.calendar

import pl.newicom.dddd.aggregate.EntityId

case class CalendarView(calendarId: EntityId,
                        summary: String,
                        description: Option[String],
                        location: Option[String],
                        timeZone: Option[String],
                        deleted: Boolean
                       )
