package ecommerce.scheduling.view.calendar

import ecommerce.scheduling.{CalendarCreated, CalendarDeleted, DescriptionUpdated, NotificationSupport, SummaryUpdated}
import org.json4s.{Extraction, Formats}
import org.json4s.JsonAST.JInt
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._
import pl.newicom.dddd.messaging.event.OfficeEventMessage
import pl.newicom.dddd.view.sql.Projection
import pl.newicom.dddd.view.sql.Projection.ProjectionAction
import slick.dbio.{DBIOAction, Effect}
import slick.dbio.Effect.Write
import slick.sql.SqlStreamingAction

import scala.concurrent.ExecutionContext

class CalendarProjection(calendarDao: CalendarDao, notificationSupport: NotificationSupport)(implicit ec: ExecutionContext, formats: Formats) extends Projection {

  def notifyEvent(eventMessage: OfficeEventMessage, eventType: String): SqlStreamingAction[Vector[Boolean], Boolean, Effect] =
    notificationSupport.notifyEvent(
      CalendarProjection.notifyChannel,
      compact(
        render(
          ("event" -> eventType) ~ ("payload" -> Extraction.decompose(eventMessage.event)) ~ ("sequenceNr" -> eventMessage.eventNumber)
        )
      )
    )

  override def consume(eventMessage: OfficeEventMessage): ProjectionAction[Write] = {
    eventMessage.event match {

      case CalendarCreated(calendarId, summary) =>
        val createOrUpdate = calendarDao.createOrUpdate(
          CalendarView(calendarId, summary, None, None, None, deleted = false)
        )

        createOrUpdate >> notifyEvent(eventMessage, "CalendarCreated")

      case SummaryUpdated(calendarId, summary) =>
        val updateSummary = calendarDao.updateSummary(calendarId, summary)

        updateSummary >> notifyEvent(eventMessage, "SummaryUpdated")

      case DescriptionUpdated(calendarId, description) =>
        val updateDescription = calendarDao.updateDescription(calendarId, if (description.isEmpty) None else Some(description))

        updateDescription >> notifyEvent(eventMessage, "DescriptionUpdated")

      case CalendarDeleted(calendarId) =>
        val updateDescription = calendarDao.updateDeleted(calendarId, deleted = true)

        updateDescription >> notifyEvent(eventMessage, "CalendarDeleted")

      case _ =>
        // TODO handle
        DBIOAction.successful(())
    }
  }
}

object CalendarProjection {

  val notifyChannel = "events"

}