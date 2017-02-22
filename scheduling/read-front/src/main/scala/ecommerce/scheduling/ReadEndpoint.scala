package ecommerce.scheduling

import akka.NotUsed
import akka.stream.scaladsl.Source
import org.json4s.Formats
import pl.newicom.dddd.http.Endpoint
import slick.jdbc.JdbcBackend

abstract class ReadEndpoint(implicit formats: Formats) extends Endpoint[JdbcBackend#DatabaseDef] {
  type Database = JdbcBackend#DatabaseDef
}