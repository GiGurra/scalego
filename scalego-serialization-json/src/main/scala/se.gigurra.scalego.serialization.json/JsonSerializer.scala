package se.gigurra.scalego.serialization.json

import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, Extraction, Formats}
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.jackson.JsonMethods.{pretty => prty}
import Extraction.{decompose, extract}
import se.gigurra.scalego.core.{ECS, Types}
import se.gigurra.scalego.serialization.{ECSSerializer, KnownSubTypes}
import ECSSerializer._

/**
  * Created by johan on 2016-09-17.
  */
case class JsonSerializer[T_Types <: Types](knownSubtypes: KnownSubTypes = KnownSubTypes.empty,
                                            jsonFormats: Formats = DefaultFormats)(
                                            implicit idTypeMapper: IdTypeMapper[T_Types#SystemId, T_Types#EntityId]) {

  private val serializer = new ECSSerializer[JValue, T_Types](new JsonMapper[T_Types](jsonFormats) {
    override def intermediary2SystemId(id: String): T_Types#SystemId = idTypeMapper.intermediary2SystemId(id)
    override def intermediary2EntityId(id: String): T_Types#EntityId = idTypeMapper.intermediary2EntityId(id)
    override def systemId2Intermediary(id: T_Types#SystemId): String = idTypeMapper.compId2Intermediary(id)
    override def entityId2Intermediary(id: T_Types#EntityId): String = idTypeMapper.entityId2Intermediary(id)
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
  def intermediary2SystemId(id: String): SystemIdType
  def intermediary2EntityId(id: String): EntityIdType
  def compId2Intermediary(id: SystemIdType): String
  def entityId2Intermediary(id: EntityIdType): String
}

object IdTypeMapper {

  implicit val longIdMapper: IdTypeMapper[Long, Long] = new IdTypeMapper[Long, Long] {
    override def compId2Intermediary(id: Long): String = id.toString
    override def intermediary2SystemId(id: String): Long = id.toLong
    override def entityId2Intermediary(id: Long): String = id.toString
    override def intermediary2EntityId(id: String): Long = id.toLong
  }

  implicit val stringIdMapper: IdTypeMapper[String, String] = new IdTypeMapper[String, String] {
    override def compId2Intermediary(id: String): String = id
    override def intermediary2SystemId(id: String): String = id
    override def entityId2Intermediary(id: String): String = id
    override def intermediary2EntityId(id: String): String = id
  }
}
