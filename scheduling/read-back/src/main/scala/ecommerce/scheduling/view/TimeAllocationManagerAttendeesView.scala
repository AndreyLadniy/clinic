package ecommerce.scheduling.view

import java.time.OffsetDateTime

import pl.newicom.dddd.aggregate.EntityId

case class TimeAllocationManagerAttendeesView(
                                               timeAllocationManagerId: EntityId,
                                               attendeeId: EntityId,
                                               organizerId: EntityId,
                                               accepted: Boolean,
                                               start: OffsetDateTime,
                                               end: OffsetDateTime,
                                               responseStatus: String)
