package ecommerce.scheduling.app

import akka.actor._
import akka.kernel.Bootable
import ecommerce.scheduling.calendar.Calendar
import ecommerce.scheduling.{CalendarTimeManager, TimeAllocationManager}
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate._
import pl.newicom.dddd.eventhandling.NoPublishing
import pl.newicom.dddd.monitoring.AggregateRootMonitoring

trait SchedulingBackendConfiguration {
  this: Bootable =>

//  val calendarTimeManagerOfficeActor = system.actorOf(CalendarTimeManagerOfficeActor.props, CalendarTimeManagerOfficeActor.Name)

  implicit object CalendarARFactory extends AggregateRootActorFactory[Calendar] {
    override def props(pc: PassivationConfig) = Props(new Calendar(pc) with NoPublishing with AggregateRootMonitoring)
  }

  implicit object TimeManagerARFactory extends AggregateRootActorFactory[CalendarTimeManager] {
    override def props(pc: PassivationConfig) = Props(new CalendarTimeManager(pc) with NoPublishing with AggregateRootMonitoring)
  }

  implicit object CalendarTimeAllocationARFactory extends AggregateRootActorFactory[TimeAllocationManager] {
    override def props(pc: PassivationConfig) = Props(new TimeAllocationManager(pc) with NoPublishing with AggregateRootMonitoring)
  }

}
