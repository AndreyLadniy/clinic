package com.rudux.dddd.view.mongodb

/** A phantom type used as the streaming result type for DBIOActions that do not support streaming.
  * Note that this is a supertype of `Streaming` (and it is used in covariant position),
  * so that any streaming action can be used where a non-streaming action is expected. */
sealed trait NoStream

/** A phantom type used as the streaming result type for DBIOActions that do support streaming. */
sealed trait Streaming[+T] extends NoStream

trait MongoDBIOAction[+R, +S <: NoStream, -E <: Effect] {

}

trait Effect

object Effect {
  /** Effect for DBIOActions that read from the database ("DQL") */
  trait Read extends Effect
  /** Effect for DBIOActions that write to the database ("DML") */
  trait Write extends Effect
  /** Effect for DBIOActions that manipulate a database schema ("DDL") */
//  trait Schema extends Effect
//  /** Effect for transactional DBIOActions ("DTL") */
//  trait Transactional extends Effect
//
//  /** The bottom type of all standard effects. It is used by the `DBIO` and `StreamingDBIO`
//    * type aliases instead of `Nothing` because the compiler does not properly infer `Nothing`
//    * where needed. You can still introduce your own custom effect types but they will not be
//    * used by `DBIO` and `StreamingDBIO`, so you either have to define your own type aliases
//    * or spell out the proper `DBIOAction` types in type annotations. */
//  trait All extends Read with Write with Schema with Transactional

    trait All extends Read with Write
}
