package ecommerce.scheduling.view.calendar

import ecommerce.scheduling.{CalendarCreated, NotificationSupport}
import org.json4s.Formats
import org.json4s.JsonAST.JInt
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._
import pl.newicom.dddd.messaging.event.OfficeEventMessage
import pl.newicom.dddd.view.sql.Projection
import pl.newicom.dddd.view.sql.Projection.ProjectionAction
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write

import scala.concurrent.ExecutionContext

class CalendarProjection(calendarDao: CalendarDao, notificationSupport: NotificationSupport)(implicit ec: ExecutionContext, formats: Formats) extends Projection {

  override def consume(eventMessage: OfficeEventMessage): ProjectionAction[Write] = {
    eventMessage.event match {

      case event @ CalendarCreated(calendarId, summary) =>
        val createOrUpdate = calendarDao.createOrUpdate(
          CalendarView(calendarId, summary, None, None, None, deleted = false)
        )

        val notifyEvent = notificationSupport.notifyEvent(
          "CalendarProjection",
          compact(
            render(
              ("event" -> "CalendarCreated") ~ ("payload" -> write(event)) ~ ("sequenceNr" -> eventMessage.eventNumber)
            )
          )
        )

        createOrUpdate >> notifyEvent

      case _ =>
        // TODO handle
        DBIOAction.successful(())
    }
  }
}