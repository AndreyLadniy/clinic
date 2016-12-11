package ecommerce.scheduling.app

import akka.actor._
import akka.kernel.Bootable
import ecommerce.scheduling.OrganizerReservation
import ecommerce.scheduling.reservation.AttendeeReservation
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.aggregate._
import pl.newicom.dddd.eventhandling.NoPublishing
import pl.newicom.dddd.monitoring.AggregateRootMonitoring

trait SchedulingBackendConfiguration {
  this: Bootable =>

  implicit object ReservationARFactory extends AggregateRootActorFactory[AttendeeReservation] {
    override def props(pc: PassivationConfig) = Props(new AttendeeReservation(pc) with NoPublishing with AggregateRootMonitoring)
  }

  implicit object OrganizerReservationARFactory extends AggregateRootActorFactory[OrganizerReservation] {
    override def props(pc: PassivationConfig) = Props(new OrganizerReservation(pc) with NoPublishing with AggregateRootMonitoring)
  }
}
