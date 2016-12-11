package ecommerce.headquarters.processes

import ecommerce.headquarters.app.HeadquartersConfiguration.department
import ecommerce.headquarters.processes.InvitingProcessManager.InvitingStatus
import ecommerce.scheduling.reservation.{ReserveInterval, _}
import ecommerce.scheduling.{AttendeeInvited, ReservationCreated, _}
import pl.newicom.dddd.actor.PassivationConfig
import pl.newicom.dddd.process._
import pl.newicom.dddd.saga.ProcessConfig

object InvitingProcessManager extends SagaSupport {

  sealed trait InvitingStatus extends SagaState[InvitingStatus] {
    def isNew = false
  }
  case object New extends InvitingStatus {
    override def isNew: Boolean = true
  }
//  case object WaitingForPayment  extends OrderStatus
//  case object DeliveryInProgress extends OrderStatus
//  case object Completed          extends OrderStatus
//  case object Failed             extends OrderStatus

  implicit object InvitingProcessConfig extends ProcessConfig[InvitingProcessManager]("inviting", department) {
    def correlationIdResolver = {
      case ReservationCreated(reservationId, _, _,_ ) => reservationId
      case AttendeeInvited(reservationId, _, _, _) => reservationId // orderId
    }
  }

}

import ecommerce.headquarters.processes.InvitingProcessManager._

class InvitingProcessManager(val pc: PassivationConfig) extends ProcessManager[InvitingStatus] {

  val officeId = InvitingProcessConfig

  def processCollaborators = List(
//    schedulingOfficeId(HeadquartersConfiguration.department),
    OrganizerReservationOfficeId, AttendeeReservationOfficeId
  )

  startWhen {

    case _: ReservationCreated => New

  } andThen {

    case New => {

      case AttendeeInvited(reservationId, attendee, start, end) =>
        ⟶ (ReserveInterval(attendee, reservationId, start, end))
    }

  }

}
