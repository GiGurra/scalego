package se.gigurra.scalego.core

import scala.collection.mutable
import scala.language.implicitConversions
import scala.reflect.ClassTag

class System[ComponentType : ClassTag, T_Types <: Types](val typeInfo: ComponentTypeInfo[ComponentType, T_Types])
                                                        (private val backingStorage: mutable.Map[T_Types#EntityId, ComponentType]) {

  def this(systemId: T_Types#SystemId, backingStorage: mutable.Map[T_Types#EntityId, ComponentType]) = this(new ComponentTypeInfo[ComponentType, T_Types](systemId))(backingStorage)

  override def equals(other: Any): Boolean = {
    other match {
      case other: System[_, _] => typeInfo == other.typeInfo && backingStorage == other.backingStorage
      case _ => false
    }
  }
}

object System {
  implicit def system2map[ComponentType, T_Types <: Types](system: System[ComponentType, T_Types]): mutable.Map[T_Types#EntityId, ComponentType] = {
    system.backingStorage
  }
}
