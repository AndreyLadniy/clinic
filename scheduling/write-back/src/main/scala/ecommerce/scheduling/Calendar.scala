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

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  def emailAddress(e: String): Boolean = {
    if (e == null) false
    else if (e.trim.isEmpty) false
    else emailRegex.findFirstMatchIn(e).isDefined
  }

  implicit case object Uninitialized extends CalendarActions with Uninitialized[CalendarActions] {

    def actions: Actions =
      handleCommands {
        case CreateCalendar(calendarId, summary) if !emailAddress(calendarId) =>
          error(s"caledarId must be valid e-mail")
        case CreateCalendar(calendarId, summary) =>
          CalendarCreated(calendarId, summary)
      }
        .handleEvents {
          case CalendarCreated(calendarId, summary) =>
            Created(new Date)
        }

  }

  case class Created(createDate: Date) extends CalendarActions {
    def actions =

      handleCommands {
        case UpdateSummary(calendarId, summary) if summary.isEmpty =>
          error(s"summary must be non-empty")
        case UpdateSummary(calendarId, summary) =>
          SummaryUpdated(calendarId, summary)
        case UpdateDescription(calendarId, description) =>
          DescriptionUpdated(calendarId, description)
        case DeleteCalendar(calendarId) =>
          CalendarDeleted(calendarId)
      }
      .handleEvents {
        case SummaryUpdated(calendarId, summary) =>
          this
        case DescriptionUpdated(calendarId, description) =>
          this
        case CalendarDeleted(calendarId) =>
          this
      }
  }


}

abstract class Calendar(val pc: PassivationConfig) extends AggregateRoot[CalendarEvent, CalendarActions, Calendar] {
  this: EventPublisher =>


}
