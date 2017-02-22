package com.rudux.dddd.view.mongodb

import akka.Done
import com.typesafe.config.Config
import pl.newicom.dddd.messaging.event.OfficeEventMessage
import pl.newicom.dddd.view.ViewHandler

import scala.concurrent.{ExecutionContext, Future}

class MongoDBViewHandler(override val config: Config, override val vuConfig: MongoDBViewUpdateConfig)
                        (implicit ex: ExecutionContext)
  extends ViewHandler(vuConfig) with MongoDBViewStoreConfiguration with FutureHelpers {

  private lazy val viewMetadataDao = new ViewMetadataDao

  def viewMetadataId = ViewMetadataId(viewName, vuConfig.office.id)

  def handle(eventMessage: OfficeEventMessage, eventNumber: Long): Future[Done] = ???
//    viewStore.run {
//      sequence(vuConfig.projections.map(_.consume(eventMessage))) >>
//      viewMetadataDao.insertOrUpdate(viewMetadataId, eventNumber)
//    }.mapToDone

  def lastEventNumber: Future[Option[Long]] = ???
//    viewStore.map {collection =>
//      collection.insert() find(viewMetadataDao.lastEventNr(viewMetadataId)).
//    }

}
