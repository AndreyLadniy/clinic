package ecommerce.headquarters.app

import akka.cluster.Cluster
import akka.kernel.Bootable
import ecommerce.headquarters.ClusterView
import ecommerce.headquarters.processes.InvitingProcessManager
import pl.newicom.dddd.cluster._
import pl.newicom.dddd.office.OfficeFactory._

class HeadquartersApp extends Bootable with HeadquartersConfiguration {

  override def startup(): Unit = {
    system.actorOf(ClusterView.props, ClusterView.Name)

    Cluster(system).registerOnMemberUp {
      office[InvitingProcessManager]
    }
  }

}

object HeadquartersApp {

  def main(args: Array[String]): Unit = {
    val app = new HeadquartersApp

    app.startup()

    sys.addShutdownHook(app.shutdown())
  }

}