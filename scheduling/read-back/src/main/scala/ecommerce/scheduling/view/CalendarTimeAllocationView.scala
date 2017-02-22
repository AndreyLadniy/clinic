package ecommerce.scheduling.view

import java.sql.Timestamp

import pl.newicom.dddd.aggregate.EntityId

case class CalendarTimeAllocationView(
                                       id: EntityId,
                                       organizerId: EntityId,
                                       start: Timestamp,
                                       end: Timestamp)
