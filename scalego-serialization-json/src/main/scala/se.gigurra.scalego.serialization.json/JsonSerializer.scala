package se.gigurra.scalego.serialization.json

import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, Extraction, Formats}
import org.json4s.jackson.JsonMethods.{compact, parse}
import org.json4s.jackson.JsonMethods.{pretty => prty}
import Extraction.{decompose, extract}
import se.gigurra.scalego.core.{ECS, IdTypes}
import se.gigurra.scalego.serialization.{ECSSerializer, IdTypeMapper, KnownSubTypes}
import ECSSerializer._

/**
  * Created by johan on 2016-09-17.
  */
case class JsonSerializer[T_IdTypes <: IdTypes](knownSubtypes: KnownSubTypes = KnownSubTypes.empty,
                                                jsonFormats: Formats = DefaultFormats)
                                               (implicit systemIdMapper: IdTypeMapper[T_IdTypes#SystemId], entityIdMapper: IdTypeMapper[T_IdTypes#EntityId]) {

  private val serializer = new ECSSerializer[JValue, T_IdTypes](new JsonMapper[T_IdTypes](jsonFormats), knownSubtypes)

  implicit class SerializableOps(ecs: ECS[T_IdTypes]) {

    def toJson(pretty: Boolean = false): String = {
      val map = serializer.SerializableECSOpsWrite(ecs).toSerializable
      if (pretty) prty(decompose(map)(jsonFormats))
      else compact(decompose(map)(jsonFormats))
    }

    def appendJson(json: String): Unit = {
      val intermediaryFormat = extract[SerializableEcs[JValue]](parse(json))(jsonFormats, implicitly[Manifest[SerializableEcs[JValue]]])
      serializer.SerializableECSOpsRead(ecs).append(intermediaryFormat)
    }
  }
}
