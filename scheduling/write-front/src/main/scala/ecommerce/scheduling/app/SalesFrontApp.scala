package ecommerce.scheduling.app

import akka.actor._
import akka.kernel.Bootable

object SchedulingFrontApp {

  def main(args: Array[String]): Unit = {
    val app = new SchedulingFrontApp

    app.startup()

    sys.addShutdownHook(app.shutdown())
  }

}

class SchedulingFrontApp extends Bootable {

  override def systemName = "scheduling-front"

  override def startup(): Unit = {
    system.actorOf(SalesFrontAppSupervisor.props, "scheduling-front-supervisor")
  }

}

object SalesFrontAppSupervisor {
  def props = Props(new SalesFrontAppSupervisor)
}

class SalesFrontAppSupervisor extends Actor with ActorLogging with SchedulingFrontConfiguration {

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  context.watch(createHttpService())

  override def receive: Receive = {
    case Terminated(ref) =>
      log.warning("Shutting down, because {} has terminated!", ref.path)
      context.system.terminate()
  }

  protected def createHttpService(): ActorRef = {
    import httpService._
    context.actorOf(HttpService.props(interface, port, timeout), "http-service")
  }
}