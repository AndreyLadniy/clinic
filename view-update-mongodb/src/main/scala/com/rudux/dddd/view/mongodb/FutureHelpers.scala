package com.rudux.dddd.view.mongodb

import akka.Done

import scala.concurrent.{ExecutionContext, Future}

trait FutureHelpers {
  implicit class PimpedFuture[T](future: Future[T])(implicit val ec: ExecutionContext) {
    def mapToUnit = future.map(_ => ())
    def mapToDone = future.map(_ => Done)
  }
}