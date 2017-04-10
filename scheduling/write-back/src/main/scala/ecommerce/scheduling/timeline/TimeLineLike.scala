package ecommerce.scheduling.timeline

trait TimeLineLike[A <: TimeInterval, Repr <: TimeLineLike[A, Repr]] {

  def add(t: A): Repr

  def remove(p: A => Boolean): Repr

  def exists(p: A => Boolean): Boolean

}
