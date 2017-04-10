package ecommerce.headquarters.app

import akka.actor.{Props, _}
import com.typesafe.config.Config
import ecommerce.headquarters.processes.TimeAllocationProcessManager
import org.slf4j.Logger
import pl.newicom.dddd.actor.{CreationSupport, PassivationConfig}
import pl.newicom.dddd.coordination.ReceptorConfig
import pl.newicom.dddd.office.LocalOfficeId
import pl.newicom.dddd.process.{Receptor, ReceptorActorFactory, SagaActorFactory}
import pl.newicom.eventstore.EventstoreSubscriber

import scala.concurrent.duration.{DurationInt, FiniteDuration}

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

  implicit object TimeAllocationProcessManagerActorFactory extends SagaActorFactory[TimeAllocationProcessManager] {
    def props(pc: PassivationConfig): Props =
      Props(new TimeAllocationProcessManager(pc))
  }

  implicit def receptorFactory[A : LocalOfficeId : CreationSupport]: ReceptorActorFactory[A] = new ReceptorActorFactory[A] {
    def receptorFactory: ReceptorFactory = (config: ReceptorConfig) => {
      new Receptor(config.copy(capacity = 100)) with EventstoreSubscriber {
        override def redeliverInterval: FiniteDuration = 10.seconds
      }
    }
  }

}
