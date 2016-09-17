package se.gigurra.scalego.serialization

import se.gigurra.scalego.core.{ComponentTypeInfo, ECS, System, Types}

import scala.language.implicitConversions
import scala.language.existentials
import scala.reflect.ClassTag
import ECSSerializer._

/**
  * Created by johan on 2016-09-17.
  */

case class ECSSerializer[IntermediaryFormat, T_Types <: Types](mapper: Mapper[IntermediaryFormat, T_Types],
                                                               knownSubtypes: KnownSubTypes = KnownSubTypes.empty) {

  import mapper._

  /////////////////////////////
  // Writing

  implicit class SerializableECSOpsWrite(ecs: ECS[T_Types]) {
    def toSerializable: SerializableEcs[IntermediaryFormat] = {
      SerializableEcs[IntermediaryFormat](ecs.systems.values.map { _.toSerializable }.toSeq)
    }
  }

  implicit class SerializableSystemOpsWrite[ComponentType: ClassTag](system: System[ComponentType, T_Types]) {
    def toSerializable: SerializableSystem[IntermediaryFormat] = {
      SerializableSystem(compId2Intermediary(system.typeInfo.id), system.map{case (k,v) => serializeComponent(k, v, system.typeInfo)}.toSeq)
    }

    private def serializeComponent(id: T_Types#EntityId, component: Any, expectedType: ComponentTypeInfo[_, _]): SerializableComponent[IntermediaryFormat] = {
      val typeId = if (component.getClass == expectedType.classTag.runtimeClass) {
        None
      } else {
        Some(knownSubtypes.class2Id.getOrElse(component.getClass, throw UnknownSubTypeForSerialization(baseType = expectedType.classTag.runtimeClass, subType = component.getClass, "serialize")))
      }
      SerializableComponent(entityId2Intermediary(id), obj2intermediary(component), subType = typeId)
    }
  }


  //////////////////////////////
  // Reading

  implicit class SerializableECSOpsRead(ecs: ECS[T_Types]) {
    def append(serializedData: SerializableEcs[IntermediaryFormat]): Unit = {
      for (serializedSystem <- serializedData.systems) {
        val system = ecs.systems.getOrElse(intermediary2CompId(serializedSystem.systemId), throw UnknownSystemForDeSerialization(serializedSystem.systemId))
        system.asInstanceOf[System[Any, T_Types]].append(serializedSystem.components)
      }
    }
  }

  implicit class SerializableSystemOpsRead(system: System[Any, T_Types]) {
    def append(serializedComponents: Seq[SerializableComponent[IntermediaryFormat]]): Unit = {
      val deserializedComponents = serializedComponents.map(_.id).zip(serializedComponents.map(deSerializeComponent(_, system.typeInfo)))
      for ((id, component) <- deserializedComponents) {
        system.put(intermediary2entityId(id), component.asInstanceOf[Any])
      }
    }

    private def deSerializeComponent(component: SerializableComponent[IntermediaryFormat], expectedType: ComponentTypeInfo[_, _]): Any = {
      val cls = component.subType match {
        case None =>
          expectedType.classTag.runtimeClass
        case Some(subTypeId) =>
          knownSubtypes.id2Class.getOrElse(subTypeId, throw UnknownSubTypeForSerialization(baseType = expectedType.classTag.runtimeClass, subType = subTypeId, "deserialize"))
      }
      intermediary2Obj(component.data, cls)
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