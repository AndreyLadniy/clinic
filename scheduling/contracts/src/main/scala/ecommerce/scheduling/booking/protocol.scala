package ecommerce.scheduling.booking

import pl.newicom.dddd.aggregate
import pl.newicom.dddd.aggregate.EntityId

sealed trait Command extends aggregate.Command {
  def reservationId: EntityId
  override def aggregateId = reservationId
}
