package se.gigurra.scalego.serialization.json

import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, Extraction, Formats}
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.jackson.JsonMethods.{pretty => prty}
import Extraction.{decompose, extract}
import se.gigurra.scalego.core.{ECS, Types}
import se.gigurra.scalego.serialization.{ECSSerializer, KnownSubtypes, SerializableEcs}

/**
  * Created by johan on 2016-09-17.
  */
case class JsonSerializer[T_Types <: Types](knownSubtypes: KnownSubtypes = KnownSubtypes.empty,
                                            jsonFormats: Formats = DefaultFormats)(
                                            implicit idTypeMapper: IdTypeMapper[T_Types#SystemId, T_Types#EntityId]) {

  private val serializer = new ECSSerializer[JValue, T_Types](new JsonMapper[T_Types](jsonFormats) {
    override def intermediary2CompId(id: String): T_Types#SystemId = idTypeMapper.intermediary2CompId(id)
    override def intermediary2entityId(id: String): T_Types#EntityId = idTypeMapper.intermediary2entityId(id)
  }, knownSubtypes)

  /////////////////////////////
  // Writing

  implicit class SerializableOpsWrite(ecs: ECS[T_Types]) {
    def toJson(pretty: Boolean = false): String = {
      val map = serializer.SerializableECSOpsWrite(ecs).toSerializable
      if (pretty) prty(decompose(map)(jsonFormats))
      else compact(decompose(map)(jsonFormats))
    }
  }


  //////////////////////////////
  // Reading

  implicit class SerializableOpsRead(ecs: ECS[T_Types]) {
    def appendJson(json: String): Unit = {
      val intermediaryFormat = extract[SerializableEcs[JValue]](parse(json))(jsonFormats, implicitly[Manifest[SerializableEcs[JValue]]])
      serializer.SerializableECSOpsRead(ecs).append(intermediaryFormat)
    }
  }
}

trait IdTypeMapper[SystemIdType, EntityIdType] {
  def intermediary2CompId(id: String): SystemIdType
  def intermediary2entityId(id: String): EntityIdType
}

object IdTypeMapper {

  implicit val longIdMapper: IdTypeMapper[Long, Long] = new IdTypeMapper[Long, Long] {
    override def intermediary2CompId(id: String): Long = id.toLong
    override def intermediary2entityId(id: String): Long = id.toLong
  }

  implicit val stringIdMapper: IdTypeMapper[String, String] = new IdTypeMapper[String, String] {
    override def intermediary2CompId(id: String): String = id
    override def intermediary2entityId(id: String): String = id
  }
}
