package ecommerce.scheduling

import com.typesafe.config.Config
import ecommerce.scheduling.view.calendar.{CalendarDao, CalendarProjection}
import ecommerce.scheduling.view.{CalendarTimeAllocationProjection, TimeAllocationManagerAttendeesDao, TimeAllocationManagerDao}
import org.json4s.Formats
import pl.newicom.dddd.serialization.JsonSerHints.fromConfig
import pl.newicom.dddd.view.sql.{SqlViewStore, SqlViewUpdateConfig, SqlViewUpdateService}
import pl.newicom.eventstore.{EventSourceProvider, EventStoreProvider}
import slick.dbio.DBIO

class SchedulingViewUpdateService(viewStore: SqlViewStore)(override implicit val profile: PostgresProfileWithDateTimeSupport)
  extends SqlViewUpdateService(viewStore)  with EventStoreProvider with EventSourceProvider {

  lazy val timeAllocationManagerDao = new TimeAllocationManagerDao

  lazy val timeAllocationManagerAttendeesDao = new TimeAllocationManagerAttendeesDao

  lazy val calendarDao = new CalendarDao

  lazy val notificationSupport = new NotificationSupport

  implicit val formats: Formats = fromConfig(config)

  def config: Config = context.system.settings.config

  override def vuConfigs: Seq[SqlViewUpdateConfig] = {
    List(
      SqlViewUpdateConfig("scheduling-calendar-time-allocation", TimeAllocationManagerOfficeId, new CalendarTimeAllocationProjection(timeAllocationManagerDao, timeAllocationManagerAttendeesDao, notificationSupport)),
      SqlViewUpdateConfig("scheduling-calendar", CalendarOfficeId, new CalendarProjection(calendarDao, notificationSupport))
    )
  }

  override def viewUpdateInitAction: DBIO[Unit] = {
      super.viewUpdateInitAction >>
        timeAllocationManagerDao.ensureSchemaCreated >> timeAllocationManagerAttendeesDao.ensureSchemaCreated >> calendarDao.ensureSchemaCreated
  }
}