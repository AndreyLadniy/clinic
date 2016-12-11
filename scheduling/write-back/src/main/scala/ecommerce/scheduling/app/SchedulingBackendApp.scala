package ecommerce.scheduling.app

import akka.cluster.Cluster
import akka.kernel.Bootable
import ecommerce.scheduling.OrganizerReservation
import ecommerce.scheduling.reservation.AttendeeReservation
import pl.newicom.dddd.cluster._
import pl.newicom.dddd.office.OfficeFactory.office

class SchedulingBackendApp extends Bootable with SchedulingBackendConfiguration {

  override def startup(): Unit = {
    Cluster(system).registerOnMemberUp {
      office[AttendeeReservation]
      office[OrganizerReservation]
    }
  }

}

object SchedulingBackendApp {

  def main(args: Array[String]): Unit = {
    val app = new SchedulingBackendApp

    app.startup()

    sys.addShutdownHook(app.shutdown())
  }

}