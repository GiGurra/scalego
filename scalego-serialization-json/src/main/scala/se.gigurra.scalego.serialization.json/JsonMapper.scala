package se.gigurra.scalego.serialization.json

import org.json4s.Formats
import se.gigurra.scalego.serialization.Mapper
import org.json4s.DefaultFormats
import org.json4s.Extraction._
import org.json4s.JsonAST.JValue
import se.gigurra.scalego.core.Types

/**
  * Created by johan on 2016-09-17.
  */
abstract class JsonMapper[T_Types <: Types](val jsonFormats: Formats = DefaultFormats) extends Mapper[JValue, T_Types] {
  override def obj2intermediary(obj: Any): JValue = decompose(obj)(jsonFormats)
  override def intermediary2Obj(intermediary: JValue, cls: Class[_]): Any = extract[Any](intermediary)(jsonFormats, Manifest.classType(cls))
  override def systemId2Intermediary(id: T_Types#SystemId): String
  override def entityId2Intermediary(id: T_Types#EntityId): String
  override def intermediary2SystemId(id: String): T_Types#SystemId
  override def intermediary2EntityId(id: String): T_Types#EntityId
}
