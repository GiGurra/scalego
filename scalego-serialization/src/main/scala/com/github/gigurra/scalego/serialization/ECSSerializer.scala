package com.github.gigurra.scalego.serialization

import com.github.gigurra.scalego.core.{ComponentTypeInfo, ECS, System, IdTypes}

import scala.language.implicitConversions
import scala.language.existentials
import scala.reflect.ClassTag
import ECSSerializer._

/**
  * Created by johan on 2016-09-17.
  */

case class ECSSerializer[IntermediaryFormat, T_IdTypes <: IdTypes](objectMapper: ObjectMapper[IntermediaryFormat, T_IdTypes],
                                                                 knownSubtypes: KnownSubTypes = KnownSubTypes.empty)
                                                                (implicit systemIdMapper: IdTypeMapper[T_IdTypes#SystemId], entityIdMapper: IdTypeMapper[T_IdTypes#EntityId]) {

  /////////////////////////////
  // Writing

  implicit class SerializableECSOpsWrite(ecs: ECS[T_IdTypes]) {
    def toSerializable: SerializableEcs[IntermediaryFormat] = {
      SerializableEcs[IntermediaryFormat](ecs.systems.values.map { _.toSerializable }.toSeq)
    }
  }

  implicit class SerializableSystemOpsWrite[ComponentType: ClassTag](system: System[ComponentType, T_IdTypes]) {
    def toSerializable: SerializableSystem[IntermediaryFormat] = {
      SerializableSystem(systemIdMapper.id2Intermediary(system.typeInfo.id), system.map{case (k,v) => serializeComponent(k, v, system.typeInfo)}.toSeq)
    }

    private def serializeComponent(id: T_IdTypes#EntityId, component: Any, expectedType: ComponentTypeInfo[_, _]): SerializableComponent[IntermediaryFormat] = {
      val typeId = if (component.getClass == expectedType.classTag.runtimeClass) {
        None
      } else {
        Some(knownSubtypes.class2Id.getOrElse(component.getClass, throw UnknownSubTypeForSerialization(baseType = expectedType.classTag.runtimeClass, subType = component.getClass, "serialize")))
      }
      SerializableComponent(entityIdMapper.id2Intermediary(id), objectMapper.obj2intermediary(component), subType = typeId)
    }
  }


  //////////////////////////////
  // Reading

  implicit class SerializableECSOpsRead(ecs: ECS[T_IdTypes]) {
    def append(serializedData: SerializableEcs[IntermediaryFormat]): Unit = {
      for (serializedSystem <- serializedData.systems) {
        val system = ecs.systems.getOrElse(systemIdMapper.intermediary2Id(serializedSystem.systemId), throw UnknownSystemForDeSerialization(serializedSystem.systemId))
        system.asInstanceOf[System[Any, T_IdTypes]].append(serializedSystem.components)
      }
    }
  }

  implicit class SerializableSystemOpsRead(system: System[Any, T_IdTypes]) {
    def append(serializedComponents: Seq[SerializableComponent[IntermediaryFormat]]): Unit = {
      val deserializedComponents = serializedComponents.map(_.id).zip(serializedComponents.map(deSerializeComponent(_, system.typeInfo)))
      for ((id, component) <- deserializedComponents) {
        system.put(entityIdMapper.intermediary2Id(id), component)
      }
    }

    private def deSerializeComponent(component: SerializableComponent[IntermediaryFormat], expectedType: ComponentTypeInfo[_, _]): Any = {
      val cls = component.subType match {
        case None =>
          expectedType.classTag.runtimeClass
        case Some(subTypeId) =>
          knownSubtypes.id2Class.getOrElse(subTypeId, throw UnknownSubTypeForSerialization(baseType = expectedType.classTag.runtimeClass, subType = subTypeId, "deserialize"))
      }
      objectMapper.intermediary2Obj(component.data, cls)
    }
  }

}

//////////////////////////////
// Common

object ECSSerializer {

  case class SerializableEcs[IntermediaryFormat](systems: Seq[SerializableSystem[IntermediaryFormat]])
  case class SerializableSystem[IntermediaryFormat](systemId: String, components: Seq[SerializableComponent[IntermediaryFormat]])
  case class SerializableComponent[IntermediaryFormat](id: String, data: IntermediaryFormat, subType: Option[String])

  class SerializationException(msg: String) extends RuntimeException(msg)

  case class UnknownSubTypeForSerialization(baseType: Class[_], subType: Any, op: String)
    extends SerializationException(s"Failed to $op unregistered/unknown type ($subType). Expected type $baseType or any of it's known subtypes")

  case class UnknownSystemForDeSerialization(systemId: Any)
    extends SerializationException(s"Failed deserialize components for system '$systemId' - No such system is created locally!")

}