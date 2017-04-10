package collection

import scala.language.higherKinds
/*
* original package strawman.collection.immutable
* */

/** Base trait for instances that can construct a collection from an iterable */
trait FromIterable[+C[X] <: Iterable[X]] {
  def fromIterable[B](it: Iterable[B]): C[B]
}


/** Base trait for companion objects of collections */
trait IterableFactories[+C[X] <: Iterable[X]] extends FromIterable[C] {

  def empty[A]: C[A] = fromIterable(Seq.empty[A])

  def apply[A](xs: A*): C[A] = fromIterable(xs.toIterable)

  def fill[A](n: Int)(elem: => A): C[A] = fromIterable(Iterator.fill(n)(elem).toIterable)

  def newBuilder[A]: scala.collection.mutable.Builder[A, C[A]]

  implicit def canBuild[A]: () => scala.collection.mutable.Builder[A, C[A]] = () => newBuilder[A] // TODO Reuse the same instance

}
