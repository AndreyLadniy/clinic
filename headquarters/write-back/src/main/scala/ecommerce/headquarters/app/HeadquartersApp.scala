package ecommerce.headquarters.app

import akka.cluster.Cluster
import akka.kernel.Bootable
import ecommerce.headquarters.processes.TimeAllocationProcessManager
import pl.newicom.dddd.cluster._
import pl.newicom.dddd.office.OfficeFactory._

class HeadquartersApp extends Bootable with HeadquartersConfiguration with ShardingSupport {

  override def startup(): Unit = {

    Cluster(system).registerOnMemberUp {
      office[TimeAllocationProcessManager]
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

//trait SingletonSupport {
//
//  def system: ActorSystem
//
//  implicit object SingletonManagerFactory extends CreationSupport {
//
//    override def getChild(name: String): Option[ActorRef] = throw new UnsupportedOperationException
//
//    override def createChild(props: Props, name: String): ActorRef = {
//      val singletonManagerName: String = s"singletonOf$name"
//      val managerSettings = ClusterSingletonManagerSettings(system).withSingletonName(name).withRole(HeadquartersConfiguration.department)
//      system.actorOf(
//        ClusterSingletonManager.props(
//          singletonProps = props,
//          terminationMessage = PoisonPill,
//          managerSettings
//        ),
//        name = singletonManagerName)
//
//      val proxySettings = ClusterSingletonProxySettings(system).withSingletonName(name)
//      system.actorOf(
//        ClusterSingletonProxy.props(
//          singletonManagerPath = s"/user/$singletonManagerName",
//          proxySettings),
//        name = s"${name}Proxy")
//    }
//  }
//
//
//}