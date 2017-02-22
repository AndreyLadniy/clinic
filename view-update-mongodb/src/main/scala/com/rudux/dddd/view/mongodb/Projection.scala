package com.rudux.dddd.view.mongodb

import com.rudux.dddd.view.mongodb.Projection.ProjectionAction
import pl.newicom.dddd.messaging.event.OfficeEventMessage
import reactivemongo.api.commands.CollectionCommand

object Projection {
  type ProjectionAction = CollectionCommand
}

trait Projection {

  def consume(event: OfficeEventMessage): ProjectionAction

}
