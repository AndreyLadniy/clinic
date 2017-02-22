package ecommerce.headquarters.app

import akka.actor.{Props, _}
import com.typesafe.config.Config
import ecommerce.headquarters.processes.TimeAllocationProcessManager
import org.slf4j.Logger
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.coordination.ReceptorConfig
import pl.newicom.dddd.process.ReceptorSupport.ReceptorFactory
import pl.newicom.dddd.process.{Receptor, SagaActorFactory}
import pl.newicom.eventstore.EventstoreSubscriber

import scala.concurrent.duration.DurationInt

object HeadquartersConfiguration {
  val department: String = "Headquarters"
}

trait HeadquartersConfiguration {

  def log: Logger
  def config: Config
  implicit def system: ActorSystem

//  def creationSupport = implicitly[CreationSupport]

//  implicit val schedulingOfficeID: LocalOfficeId[Scheduler] = dddd.scheduling.schedulingOfficeId(department)

//  implicit object SchedulerFactory extends AggregateRootActorFactory[Scheduler] {
//    override def props(pc: PassivationConfig) = Props(new Scheduler(pc) with NoPublishing {
//      override def id = "global"
//    })
//  }

  implicit object OrderProcessManagerActorFactory extends SagaActorFactory[TimeAllocationProcessManager] {
    def props(pc: PassivationConfig): Props =
      Props(new TimeAllocationProcessManager(pc))
  }

  implicit def receptorFactory: ReceptorFactory = (config: ReceptorConfig) => {
    new Receptor(config.copy(capacity = 100)) with EventstoreSubscriber {
      override def redeliverInterval = 10.seconds
    }
  }

}
