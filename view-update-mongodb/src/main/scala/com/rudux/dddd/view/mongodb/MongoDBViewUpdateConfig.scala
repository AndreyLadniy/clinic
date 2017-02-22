package com.rudux.dddd.view.mongodb

import pl.newicom.dddd.aggregate.BusinessEntity
import pl.newicom.dddd.view.ViewUpdateConfig

case class MongoDBViewUpdateConfig(
                                override val viewName: String,
                                override val office: BusinessEntity,
                                projections: Projection*)
  extends ViewUpdateConfig