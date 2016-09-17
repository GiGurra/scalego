package se.gigurra.scalego.serialization

import se.gigurra.scalego.core.{ComponentTypeInfo, ECS, System, Types}

import scala.language.implicitConversions
import scala.language.existentials
import scala.reflect.ClassTag

/**
  * Created by johan on 2016-09-17.
  */
trait Mapper[IntermediaryFormat, T_Types <: Types] {
  def obj2intermediary(obj: Any): IntermediaryFormat
  def intermediary2Obj(obj: IntermediaryFormat, cls: Class[_]): Any
  def compId2Intermediary(id: T_Types#SystemId): String
  def intermediary2CompId(id: String): T_Types#SystemId
  def entityId2Intermediary(id: T_Types#EntityId): String
  def intermediary2entityId(id: String): T_Types#EntityId
}

case class ECSSerializer[IntermediaryFormat, T_Types <: Types](mapper: Mapper[IntermediaryFormat, T_Types],
                                                               knownSubtypes: KnownSubtypes = KnownSubtypes.empty) {

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

case class SerializableEcs[IntermediaryFormat](systems: Seq[SerializableSystem[IntermediaryFormat]])
case class SerializableSystem[IntermediaryFormat](systemId: String, components: Seq[SerializableComponent[IntermediaryFormat]])
case class SerializableComponent[IntermediaryFormat](id: String, data: IntermediaryFormat, subType: Option[String])

case class KnownSubtypes(mappings: Set[(String, Class[_])]) {
  val id2Class: Map[String, Class[_]] = mappings.toMap
  val class2Id: Map[Class[_], String] = id2Class.map(_.swap)
  def +(newMappings: Set[(String, Class[_])]): KnownSubtypes = KnownSubtypes(mappings ++ newMappings)
  def +(newFormats: KnownSubtypes): KnownSubtypes = KnownSubtypes(mappings ++ newFormats.mappings)
  def +(newMapping: (String, Class[_])): KnownSubtypes = KnownSubtypes(mappings + newMapping)
}

object KnownSubtypes {
  def apply(mappings: (String, Class[_])*): KnownSubtypes = new KnownSubtypes(mappings.toSet)
  def fromShortClassName(types: Class[_]*): KnownSubtypes = apply(types.map(t => t.getSimpleName -> t): _*)
  def fromFullClassName(types: Class[_]*): KnownSubtypes = apply(types.map(t => t.getName -> t): _*)
  val empty: KnownSubtypes = apply()
}

class SerializationException(msg: String) extends RuntimeException(msg)

case class UnknownSubTypeForSerialization(baseType: Class[_], subType: Any, op: String)
  extends SerializationException(s"Failed to $op unregistered/unknown type ($subType). Expected type $baseType or any of it's known subtypes")

case class UnknownSystemForDeSerialization(systemId: Any)
  extends SerializationException(s"Failed deserialize components for system '$systemId' - No such system is created locally!")
