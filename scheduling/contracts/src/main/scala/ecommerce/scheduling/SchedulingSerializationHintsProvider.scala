package ecommerce.scheduling

import java.time.{LocalDate, ZonedDateTime}

import org.json4s.JsonAST.{JNull, JString}
import org.json4s.{CustomSerializer, Formats, NoTypeHints}
import pl.newicom.dddd.serialization.{JsonExtraSerHints, JsonSerializationHintsProvider}

class SchedulingSerializationHintsProvider extends JsonSerializationHintsProvider {

  case object ZonedDateTimeSerializer extends CustomSerializer[ZonedDateTime](format =>
    (
      {
        case JString(s) =>
          ZonedDateTime.parse(s)
        case JNull => null
      },
      {
        case d: ZonedDateTime => JString(d.toString)
      }
    )
  )

  case object LocalDateSerializer extends CustomSerializer[LocalDate](format =>
    (
      {
        case JString(s) =>
          LocalDate.parse(s)
        case JNull => null
      },
      {
        case d: LocalDate => JString(d.toString)
      }
    )
  )

  object JavaTimeSerializers {
    val all = List(ZonedDateTimeSerializer, LocalDateSerializer)
  }

  val serializers = JavaTimeSerializers.all

  override def hints(default: Formats) = JsonExtraSerHints(NoTypeHints, serializers)
}
