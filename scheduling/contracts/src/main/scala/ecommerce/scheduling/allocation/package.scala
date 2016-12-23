package ecommerce.scheduling

import pl.newicom.dddd.office.RemoteOfficeId

package object allocation {

  implicit object TimeAllocationOfficeId extends RemoteOfficeId[allocation.TimeAllocationCommand]("TimeAllocation", "Scheduling", classOf[allocation.TimeAllocationCommand])

}
