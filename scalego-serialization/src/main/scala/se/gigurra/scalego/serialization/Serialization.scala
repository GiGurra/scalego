package se.gigurra.scalego.serialization

import se.gigurra.scalego.core.{ComponentTypeInfo, ECS, System, Types}

import scala.language.implicitConversions
import scala.language.existentials
import scala.reflect.ClassTag

/**
  * Created by johan on 2016-09-17.
  */
object Serialization {

  implicit class SerializableECSOps[T_Types <: Types](ecs: ECS[T_Types]) {
    def toSerializable(implicit formats: SerializationFormats): SerializableEcs = {
      SerializableEcs(ecs.systems.values.map { _.toSerializable }.toSeq)
    }
  }

  implicit class SerializableSystemOps[ComponentType: ClassTag, T_Types <: Types](system: System[ComponentType, T_Types]) {
    def toSerializable(implicit formats: SerializationFormats): SerializableSystem = {
      SerializableSystem(system.typeInfo.id, system.map{case (k,v) => serializeComponent(k, v, system.typeInfo)}.toSeq)
    }

    private def serializeComponent(id: Any, component: Any, expectedType: ComponentTypeInfo[_, _])(implicit formats: SerializationFormats): SerializedComponent = {
      if (component.getClass == expectedType.classTag.runtimeClass) {
        SerializedComponent(id, component, subType = None)
      } else {
        val typeId = formats.class2Id.getOrElse(component.getClass, throw UnknownSubTypeForSerialization(baseType = expectedType.classTag.runtimeClass, subType = component.getClass))
        SerializedComponent(id, component, subType = Some(typeId))
      }
    }
  }

  case class SerializableEcs(data: Seq[SerializableSystem])
  case class SerializableSystem(systemId: Any, components: Seq[SerializedComponent])
  case class SerializedComponent(id: Any, data: Any, subType: Option[String])
}

case class SerializationFormats(mappings: Set[(String, Class[_])]) {
  val id2Class: Map[String, Class[_]] = mappings.toMap
  val class2Id: Map[Class[_], String] = id2Class.map(_.swap)
  def +(newMappings: Set[(String, Class[_])]): SerializationFormats = SerializationFormats(mappings ++ newMappings)
  def +(newFormats: SerializationFormats): SerializationFormats = SerializationFormats(mappings ++ newFormats.mappings)
  def +(newMapping: (String, Class[_])): SerializationFormats = SerializationFormats(mappings + newMapping)
}

object SerializationFormats {
  def apply(mappings: (String, Class[_])*): SerializationFormats = new SerializationFormats(mappings.toSet)
  def fromShortClassName(types: Class[_]*): SerializationFormats = apply(types.map(t => t.getSimpleName -> t): _*)
  def fromFullClassName(types: Class[_]*): SerializationFormats = apply(types.map(t => t.getName -> t): _*)
  val empty: SerializationFormats = apply()
}

case class UnknownSubTypeForSerialization(baseType: Class[_], subType: Class[_])
  extends RuntimeException(s"Tried to serialize unknown type ($subType). Expected type $baseType or any of it's known subtypes")
