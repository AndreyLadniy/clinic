package ecommerce.scheduling

import pl.newicom.dddd.office.RemoteOfficeId

/**
  * Created by andrey on 11.12.16.
  */
package object reservation {

    implicit object AttendeeReservationOfficeId extends RemoteOfficeId[ecommerce.scheduling.reservation.Command]("AttendeeReservation", "Scheduling", classOf[ecommerce.scheduling.reservation.Command])

}
