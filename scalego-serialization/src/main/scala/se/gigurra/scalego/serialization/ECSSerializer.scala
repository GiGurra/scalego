package se.gigurra.scalego.serialization

import se.gigurra.scalego.core.{ComponentTypeInfo, ECS, System, Types}

import scala.language.implicitConversions
import scala.language.existentials
import scala.reflect.ClassTag

/**
  * Created by johan on 2016-09-17.
  */

case class Mapper[IntermediaryFormat](obj2intermediary: Any => IntermediaryFormat, intermediary2Obj: (IntermediaryFormat, Class[_]) => Any)

case class ECSSerializer[IntermediaryFormat](mapper: Mapper[IntermediaryFormat], formats: SerializationFormats = SerializationFormats.empty) {

  /////////////////////////////
  // Writing

  implicit class SerializableECSOpsWrite[T_Types <: Types](ecs: ECS[T_Types]) {
    def toSerializable: SerializableEcs[IntermediaryFormat] = {
      SerializableEcs[IntermediaryFormat](ecs.systems.values.map { _.toSerializable }.toSeq)
    }
  }

  implicit class SerializableSystemOpsWrite[ComponentType: ClassTag, T_Types <: Types](system: System[ComponentType, T_Types]) {
    def toSerializable: SerializableSystem[IntermediaryFormat] = {
      SerializableSystem(system.typeInfo.id, system.map{case (k,v) => serializeComponent(k, v, system.typeInfo)}.toSeq)
    }

    private def serializeComponent(id: Any, component: Any, expectedType: ComponentTypeInfo[_, _]): SerializableComponent[IntermediaryFormat] = {
      val typeId = if (component.getClass == expectedType.classTag.runtimeClass) {
        None
      } else {
        Some(formats.class2Id.getOrElse(component.getClass, throw UnknownSubTypeForSerialization(baseType = expectedType.classTag.runtimeClass, subType = component.getClass, "serialize")))
      }
      SerializableComponent(id, mapper.obj2intermediary(component), subType = typeId)
    }
  }


  //////////////////////////////
  // Reading

  implicit class SerializableECSOpsRead[T_Types <: Types](ecs: ECS[T_Types]) {
    def append(serializedData: SerializableEcs[IntermediaryFormat]): Unit = {
      for (serializedSystem <- serializedData.systems) {
        val system = ecs.systems.getOrElse(serializedSystem.systemId.asInstanceOf[T_Types#ComponentTypeId], throw UnknownSystemForDeSerialization(serializedSystem.systemId))
        system.asInstanceOf[System[Any, T_Types]].append(serializedSystem.components)
      }
    }
  }

  implicit class SerializableSystemOpsRead[T_Types <: Types](system: System[Any, T_Types]) {
    def append(serializedComponents: Seq[SerializableComponent[IntermediaryFormat]]): Unit = {
      val deserializedComponents = serializedComponents.map(_.id).zip(serializedComponents.map(deSerializeComponent(_, system.typeInfo)))
      for ((id, component) <- deserializedComponents) {
        system.put(id.asInstanceOf[T_Types#EntityId], component.asInstanceOf[Any])
      }
    }

    private def deSerializeComponent(component: SerializableComponent[IntermediaryFormat], expectedType: ComponentTypeInfo[_, _]): Any = {
      val cls = component.subType match {
        case None =>
          expectedType.classTag.runtimeClass
        case Some(subTypeId) =>
          formats.id2Class.getOrElse(subTypeId, throw UnknownSubTypeForSerialization(baseType = expectedType.classTag.runtimeClass, subType = subTypeId, "deserialize"))
      }
      mapper.intermediary2Obj.apply(component.data, cls)
    }
  }

}

//////////////////////////////
// Common

case class SerializableEcs[IntermediaryFormat](systems: Seq[SerializableSystem[IntermediaryFormat]])
case class SerializableSystem[IntermediaryFormat](systemId: Any, components: Seq[SerializableComponent[IntermediaryFormat]])
case class SerializableComponent[IntermediaryFormat](id: Any, data: IntermediaryFormat, subType: Option[String])

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

class SerializationException(msg: String) extends RuntimeException(msg)

case class UnknownSubTypeForSerialization(baseType: Class[_], subType: Any, op: String)
  extends SerializationException(s"Failed to $op unregistered/unknown type ($subType). Expected type $baseType or any of it's known subtypes")

case class UnknownSystemForDeSerialization(systemId: Any)
  extends SerializationException(s"Failed deserialize components for system '$systemId' - No such system is created locally!")
