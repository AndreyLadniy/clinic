package ecommerce.scheduling

import pl.newicom.dddd.office.RemoteOfficeId

package object invitation {

  implicit object InvitationOfficeId extends RemoteOfficeId[ecommerce.scheduling.invitation.InvitationCommand]("Invitation", "Scheduling", classOf[ecommerce.scheduling.invitation.InvitationCommand])

}
