package ecommerce.scheduling.view

import java.time.OffsetDateTime

import pl.newicom.dddd.aggregate.EntityId

case class TimeAllocationManagerView(
                                      timeAllocationManagerId: EntityId,
                                       organizerId: EntityId,
                                       start: OffsetDateTime,
                                       end: OffsetDateTime,
                                       preparedForAccepting: Boolean)
