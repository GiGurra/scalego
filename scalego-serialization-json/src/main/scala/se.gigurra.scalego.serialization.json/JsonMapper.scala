package se.gigurra.scalego.serialization.json

import org.json4s.Formats
import se.gigurra.scalego.serialization.ObjectMapper
import org.json4s.DefaultFormats
import org.json4s.Extraction._
import org.json4s.JsonAST.JValue
import se.gigurra.scalego.core.Types

/**
  * Created by johan on 2016-09-17.
  */
class JsonMapper[T_Types <: Types](val jsonFormats: Formats = DefaultFormats) extends ObjectMapper[JValue, T_Types] {
  override def obj2intermediary(obj: Any): JValue = decompose(obj)(jsonFormats)
  override def intermediary2Obj(intermediary: JValue, cls: Class[_]): Any = extract[Any](intermediary)(jsonFormats, Manifest.classType(cls))
}
