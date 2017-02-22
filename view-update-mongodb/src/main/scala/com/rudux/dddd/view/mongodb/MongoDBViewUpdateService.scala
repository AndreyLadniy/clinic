package com.rudux.dddd.view.mongodb

import pl.newicom.dddd.messaging.event.EventSourceProvider
import pl.newicom.dddd.view.ViewUpdateService

import scala.concurrent.Future

abstract class MongoDBViewUpdateService extends ViewUpdateService with FutureHelpers {
  this: MongoDBViewStoreConfiguration with EventSourceProvider=>

  type VUConfig = MongoDBViewUpdateConfig

  override def ensureViewStoreAvailable: Future[Unit] = {
    viewStore.mapToUnit
  }

//  override def onViewUpdateInit: Future[ViewUpdateInitiated.type] =
//    viewStore.map(_. runCommand(viewUpdateInitAction)) {
//      viewUpdateInitAction >> successful(ViewUpdateInitiated)
//    }
//
//  def viewUpdateInitAction: CollectionCommand =
//    new ViewMetadataDao().ensureSchemaCreated


  override def viewHandler(vuConfig: VUConfig) =
    new MongoDBViewHandler(config, vuConfig)

}
