package ecommerce.cluster

import akka.kernel.Bootable

class ClusterStartupApp extends Bootable {

  override def startup(): Unit = {
    system.actorOf(ClusterView.props, ClusterView.Name)
  }

}

object ClusterStartupApp {

  def main(args: Array[String]): Unit = {
    val app = new ClusterStartupApp

    app.startup()

    sys.addShutdownHook(app.shutdown())
  }

}