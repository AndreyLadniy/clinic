package ecommerce.scheduling

import java.time.{LocalDate, ZonedDateTime}

import pl.newicom.dddd.aggregate.EntityId

case class Calendar(id: EntityId, version: Long, email: String)

sealed trait EventInterval

case class DateTimeInterval(start: Long, end: Long)

case class DateInterval(start: LocalDate, end: LocalDate)

case class Attendee(email: String)

case class Organizer(email: String)

case class Event(id: EntityId, interval: EventInterval, organizer: Calendar, attendees: Seq[Calendar])

case class TimeReservation(id: EntityId, interval: EventInterval, organizer: Calendar, attendees: Seq[Calendar])
