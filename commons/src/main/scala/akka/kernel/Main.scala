package akka.kernel

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory._

trait Bootable {

  def systemName = "ecommerce"

  lazy val log = getLogger(getClass.getName)

  lazy val config: Config = ConfigFactory.load()

  implicit lazy val system = ActorSystem(systemName, config)

  /**
   * Callback run on microkernel startup.
   * Create initial actors and messages here.
   */
  def startup(): Unit

  /**
   * Callback run on microkernel shutdown.
   * Shutdown actor systems here.
   */
  def shutdown(): Unit = {
    system.terminate()
  }

}
