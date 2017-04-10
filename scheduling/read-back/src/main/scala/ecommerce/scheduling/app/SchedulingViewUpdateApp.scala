package ecommerce.scheduling.app

import akka.actor._
import akka.kernel.Bootable
import ecommerce.scheduling.{PostgresProfileWithDateTimeSupport, SchedulingViewUpdateService}
import pl.newicom.dddd.view.sql.SqlViewStore
import slick.driver.{JdbcProfile, PostgresDriver}

class SchedulingViewUpdateApp extends Bootable {

  override def systemName = "scheduling-view-update"

  def startup() = {
    implicit val profile = PostgresProfileWithDateTimeSupport
    system.actorOf(Props(new SchedulingViewUpdateService(new SqlViewStore(config))), "scheduling-view-update-service")
  }

}

object SchedulingViewUpdateApp {

  def main(args: Array[String]): Unit = {
    val app = new SchedulingViewUpdateApp

    app.startup()

    sys.addShutdownHook(app.shutdown())
  }

}