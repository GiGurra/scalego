package se.gigurra.scalego.serialization

import org.json4s.{DefaultFormats, Extraction, Formats}
import org.json4s.Extraction._
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods
import se.gigurra.scalego.core.Types
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.jackson.JsonMethods.{pretty => prty}

/**
  * Created by johan on 2016-09-17.
  */

object SerializationSpec extends JsonMethods {

  trait BaseType
  case class SubType(x: Int, y: Int) extends BaseType

  case class Position(x: Int, y: Int)
  case class Velocity(x: Int, y: Int)

  class StringBasedIdTypes extends Types {
    override type ComponentTypeId = String
    override type EntityId = String
  }

  class LongBasedIdTypes extends Types {
    override type ComponentTypeId = Long
    override type EntityId = Long
  }

  implicit val jsonFormats = DefaultFormats
  object JsonTestMapper extends Mapper[JValue] (
    obj2intermediary = { obj =>
      decompose(obj)
    },
    intermediary2Obj = { (jVal: JValue, cls: Class[_]) =>
      extract[Any](jVal)(jsonFormats, Manifest.classType(cls))
    }
  )

}

