package ecommerce.scheduling

import com.typesafe.config.Config
import ecommerce.scheduling.view.{AttendeeCalendarTimeAllocationDao, CalendarTimeAllocationDao, CalendarTimeAllocationProjection}
import pl.newicom.dddd.view.sql.{SqlViewUpdateConfig, SqlViewUpdateService}
import pl.newicom.eventstore.EventSourceProvider
import slick.dbio.DBIO
import slick.driver.JdbcProfile

class SchedulingViewUpdateService(override val config: Config)(override implicit val profile: JdbcProfile)
  extends SqlViewUpdateService with SalesReadBackendConfiguration with EventSourceProvider {

  lazy val calendarTimeAllocationDao = new CalendarTimeAllocationDao

  lazy val attendeeCalendarTimeAllocationDao = new AttendeeCalendarTimeAllocationDao

  override def vuConfigs: Seq[SqlViewUpdateConfig] = {
    List(
      SqlViewUpdateConfig("scheduling-calendar-time-allocation", TimeAllocationManagerOfficeId, new CalendarTimeAllocationProjection(calendarTimeAllocationDao, attendeeCalendarTimeAllocationDao))
    )
  }

  override def viewUpdateInitAction: DBIO[Unit] = {
      super.viewUpdateInitAction >>
        calendarTimeAllocationDao.ensureSchemaCreated >> attendeeCalendarTimeAllocationDao.ensureSchemaCreated
  }
}