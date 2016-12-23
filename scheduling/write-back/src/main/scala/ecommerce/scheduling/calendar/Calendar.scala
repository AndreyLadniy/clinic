package ecommerce.scheduling.calendar

import java.util.Date

import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate.{AggregateRoot, AggregateRootSupport, AggregateState, EntityId}
import pl.newicom.dddd.eventhandling.EventPublisher
import pl.newicom.dddd.office.LocalOfficeId.fromRemoteId

object Calendar extends AggregateRootSupport {

  implicit val officeId = fromRemoteId[Calendar](CalendarOfficeId)

  case class State(createDate: Date)
    extends AggregateState[State] {

    override def apply = {
      case CalendarCreated(calendarId) =>
        this
    }

  }

}

abstract class Calendar(val pc: PassivationConfig) extends AggregateRoot[Calendar.State, Calendar] {
  this: EventPublisher =>

  import Calendar.State

  override val factory: AggregateRootFactory = {
    case CalendarCreated(calendarId) =>
      State(createDate = new Date)
  }

  override def handleCommand: Receive = {
    case CreateCalendar(calendarId) =>
      if (initialized) {
        throw new RuntimeException(s"Calendar $calendarId already exists")
      } else {
        raise(CalendarCreated(calendarId))
      }
  }
}
