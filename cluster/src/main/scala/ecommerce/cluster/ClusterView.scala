package ecommerce.cluster

import akka.actor.{Actor, ActorLogging, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._


object ClusterView {

  case object GetMemberNodes

  final val Name = "cluster-view"

  def props: Props = Props(new ClusterView)
}

class ClusterView extends Actor with ActorLogging {
  import ClusterView._

  private var members = Set.empty[Address]

  Cluster(context.system).subscribe(self, InitialStateAsEvents, classOf[MemberEvent])

  override def receive: PartialFunction[Any, Unit] = {
    case GetMemberNodes =>
      sender() ! members

    case MemberJoined(member) =>
      log.info("Member joined: {}", member.address)
      members += member.address

    case MemberUp(member) =>
      log.info("Member up: {}", member.address)
      members += member.address

    case MemberRemoved(member, _) =>
      log.info("Member removed: {}", member.address)
      members -= member.address
  }
}