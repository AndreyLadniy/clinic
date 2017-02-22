package ecommerce.scheduling.view

import java.sql.Timestamp

import pl.newicom.dddd.aggregate.EntityId

case class AttendeeCalendarTimeAllocationView(
                                               id: EntityId,
                                               attendeeId: EntityId,
                                               start: Timestamp,
                                               end: Timestamp)
