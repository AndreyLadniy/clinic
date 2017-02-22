package com.rudux.dddd.view.mongodb

import com.typesafe.config.Config
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Collection, DefaultDB, MongoDriver}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

trait MongoDBViewStoreConfiguration {
  def config: Config

  val connection = MongoDriver().connection(config.getStringList("mongodb.servers").asScala)

  val database = config.getString("mongodb.database")

  val collection = config.getString("mongodb.collection")

  def viewStore(implicit ex: ExecutionContext): Future[BSONCollection] = {
    connection.database(database).map(_.collection(collection))
  }

//  def viewStore: Future[Collection] = {
//    connection.database(database).map(_.collection(collection))
//  }

}