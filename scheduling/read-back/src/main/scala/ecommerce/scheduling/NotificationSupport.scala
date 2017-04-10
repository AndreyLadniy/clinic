package ecommerce.scheduling

import scala.concurrent.ExecutionContext

class NotificationSupport(implicit val profile: PostgresProfileWithDateTimeSupport, ec: ExecutionContext)  {

  import profile.api._

  def notifyEvent(channel: String, event: String) = sql"""SELECT pg_notify('#$channel', '#$event');""".as[Boolean]

}

object NotificationSupport {

  case class EventMessage(jsonClass: String, payload: String, sequenceNr: Long)

}