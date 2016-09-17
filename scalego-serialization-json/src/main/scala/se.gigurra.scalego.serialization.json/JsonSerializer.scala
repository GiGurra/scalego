package se.gigurra.scalego.serialization.json

import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, Extraction, Formats}
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.jackson.JsonMethods.{pretty => prty}
import Extraction.{decompose, extract}
import se.gigurra.scalego.core.{ECS, Types}
import se.gigurra.scalego.serialization.{ECSSerializer, IdTypeMapper, KnownSubTypes}
import ECSSerializer._

/**
  * Created by johan on 2016-09-17.
  */
case class JsonSerializer[T_Types <: Types](knownSubtypes: KnownSubTypes = KnownSubTypes.empty,
                                            jsonFormats: Formats = DefaultFormats)
                                           (implicit systemIdMapper: IdTypeMapper[T_Types#SystemId], entityIdMapper: IdTypeMapper[T_Types#EntityId]) {

  private val serializer = new ECSSerializer[JValue, T_Types](new JsonMapper[T_Types](jsonFormats), knownSubtypes)

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
