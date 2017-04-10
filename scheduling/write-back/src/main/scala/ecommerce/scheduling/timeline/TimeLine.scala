package ecommerce.scheduling.timeline

import collection.IterableFactories
import collection.mutable.Buildable

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
//
//trait TimeLine[A <: TimeInterval] extends TimeLineLike[A, TimeLine[A]] with Buildable[A, TimeLine[A]] with Iterable[A] {
//
//  def fromIterable[B <: TimeInterval](c: Iterable[B]): TimeLine[B] = TimeLine.fromIterable(c)
//
//  override protected[this] def newBuilder: mutable.Builder[A, TimeLine[A]] = TimeLine.newBuilder
//
//}
//
//object TimeLine extends IterableFactories[TimeLine] {
//
//  override def fromIterable[B <: TimeInterval](coll: Iterable[B]): TimeLine[B] = coll.foldLeft(TimeLine[B]()){case (acc, el) => acc add el}
//
//  override def newBuilder[A <: TimeInterval]: mutable.Builder[A, TimeLine[A]] = new ListBuffer[A].mapResult(fromIterable[A](_))
//
//}