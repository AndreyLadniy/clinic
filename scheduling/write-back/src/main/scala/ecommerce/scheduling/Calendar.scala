package ecommerce.scheduling

import java.util.Date

import ecommerce.scheduling.Calendar.CalendarActions
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate._
import pl.newicom.dddd.eventhandling.EventPublisher
import pl.newicom.dddd.office.LocalOfficeId.fromRemoteId

object Calendar extends AggregateRootSupport {

  implicit val officeId = fromRemoteId[Calendar](CalendarOfficeId)

  sealed trait CalendarActions extends AggregateActions[CalendarEvent, CalendarActions] {}

  implicit case object Uninitialized extends CalendarActions with Uninitialized[CalendarActions] {

    def actions: Actions =
      handleCommands {
        case CreateCalendar(calendarId, summary) =>
          CalendarCreated(calendarId, summary)
      }
        .handleEvents {
          case CalendarCreated(calendarId, summary) =>
            Created(new Date)
        }

  }

  case class Created(createDate: Date) extends CalendarActions {
    def actions: Actions = noActions
  }


}

abstract class Calendar(val pc: PassivationConfig) extends AggregateRoot[CalendarEvent, CalendarActions, Calendar] {
  this: EventPublisher =>
}
