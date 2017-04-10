package ecommerce.gateway.api.web

import akka.kernel.Bootable
import com.typesafe.config.Config

class WebApiGatewayApp extends Bootable {

  override def systemName = "web-api-gateway"

  def startup() = {
    new WebApiGatewayConfiguration {
      override def config: Config = WebApiGatewayApp.this.config

      import httpService._
      system.actorOf(HttpService.props(interface, port, askTimeout), "http-service")
    }
  }

}

object WebApiGatewayApp {

  def main(args: Array[String]): Unit = {
    val app = new WebApiGatewayApp

    app.startup()

    sys.addShutdownHook(app.shutdown())
  }

}