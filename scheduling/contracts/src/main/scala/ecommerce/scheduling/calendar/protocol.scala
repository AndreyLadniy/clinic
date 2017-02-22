package ecommerce.scheduling.calendar

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId

sealed trait CalendarCommand extends aggregate.Command {
  def calendarId: EntityId
  override def aggregateId: EntityId = calendarId
}

case class CreateCalendar(calendarId: EntityId) extends CalendarCommand

sealed trait CalendarEvent

case class CalendarCreated(calendarId: EntityId) extends CalendarEvent