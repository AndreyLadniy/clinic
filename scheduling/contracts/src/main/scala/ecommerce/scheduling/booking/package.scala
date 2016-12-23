package ecommerce.scheduling

import pl.newicom.dddd.office.RemoteOfficeId

package object booking {

  implicit object TimeBookingOfficeId extends RemoteOfficeId[booking.Command]("TimeBooking", "Scheduling", classOf[booking.Command])

}
