package ecommerce

import pl.newicom.dddd.office.RemoteOfficeId

package object scheduling {

  implicit object OrganizerReservationOfficeId extends RemoteOfficeId[scheduling.Command]("OrganizerReservation", "Scheduling", classOf[scheduling.Command])

}
