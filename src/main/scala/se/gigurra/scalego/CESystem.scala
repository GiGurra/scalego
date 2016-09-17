package se.gigurra.scalego

import scala.collection.mutable
import scala.language.implicitConversions

class CESystem[ComponentType, T_Types <: Types](val typeInfo: ComponentTypeInfo[ComponentType, T_Types])
                                               (private val backingStorage: mutable.Map[T_Types#EntityId, ComponentType]) {

  def this(componentTypeId: T_Types#ComponentTypeId)(backingStorage: mutable.Map[T_Types#EntityId, ComponentType]) = this(new ComponentTypeInfo[ComponentType, T_Types](componentTypeId))(backingStorage)

  override def equals(other: Any): Boolean = {
    other match {
      case other: CESystem[_, _] => typeInfo == other.typeInfo && backingStorage == other.backingStorage
      case _ => false
    }
  }
}

object CESystem {
  implicit def system2map[ComponentType, T_Types <: Types](system: CESystem[ComponentType, T_Types]): mutable.Map[T_Types#EntityId, ComponentType] = {
    system.backingStorage
  }
}
