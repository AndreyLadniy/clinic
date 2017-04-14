package ecommerce.scheduling

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId

sealed trait CalendarCommand extends aggregate.Command {
  def calendarId: EntityId
  override def aggregateId: EntityId = calendarId
}

case class CreateCalendar(calendarId: EntityId, summary: String) extends CalendarCommand

case class DeleteCalendar(calendarId: EntityId) extends CalendarCommand

case class UpdateSummary(calendarId: EntityId, summary: String) extends CalendarCommand

case class UpdateDescription(calendarId: EntityId, description: String) extends CalendarCommand



sealed trait CalendarEvent

case class CalendarCreated(calendarId: EntityId, summary: String) extends CalendarEvent

case class SummaryUpdated(calendarId: EntityId, summary: String) extends CalendarEvent

case class DescriptionUpdated(calendarId: EntityId, description: String) extends CalendarEvent

case class CalendarDeleted(calendarId: EntityId) extends CalendarEvent