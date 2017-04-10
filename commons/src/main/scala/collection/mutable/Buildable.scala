package collection.mutable

import scala.collection.mutable


/*
* original in
*
* package strawman
* package collection
* */
trait Buildable[+A, +Repr] extends Any {

  /** Creates a new builder. */
  protected[this] def newBuilder: mutable.Builder[A, Repr]

}