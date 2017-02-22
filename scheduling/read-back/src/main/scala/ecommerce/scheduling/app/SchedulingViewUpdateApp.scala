package ecommerce.scheduling.app

import akka.actor._
import akka.kernel.Bootable
import ecommerce.scheduling.SchedulingViewUpdateService
import slick.driver.{JdbcProfile, PostgresDriver}

class SchedulingViewUpdateApp extends Bootable {

  override def systemName = "scheduling-view-update"

  def startup() = {
    implicit val profile: JdbcProfile = PostgresDriver
    system.actorOf(Props(new SchedulingViewUpdateService(config)), "sales-view-update-service")
  }

}

object SchedulingViewUpdateApp {

  def main(args: Array[String]): Unit = {
    val app = new SchedulingViewUpdateApp

    app.startup()

    sys.addShutdownHook(app.shutdown())
  }

}