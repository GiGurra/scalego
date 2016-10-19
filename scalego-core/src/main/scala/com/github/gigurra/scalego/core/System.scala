package com.github.gigurra.scalego.core

import scala.collection.mutable
import scala.language.implicitConversions
import scala.reflect.ClassTag

class System[ComponentType : ClassTag, T_IdTypes <: IdTypes](val typeInfo: ComponentTypeInfo[ComponentType, T_IdTypes])
                                                            (private val backingStorage: mutable.Map[T_IdTypes#EntityId, ComponentType]) {

  def this(systemId: T_IdTypes#SystemId, backingStorage: mutable.Map[T_IdTypes#EntityId, ComponentType]) = this(new ComponentTypeInfo[ComponentType, T_IdTypes](systemId))(backingStorage)

  override def hashCode(): Int = {
    typeInfo.hashCode() + backingStorage.hashCode()
  }

  override def equals(other: Any): Boolean = {
    other match {
      case other: System[_, _] =>
        getClass == other.getClass &&
          typeInfo == other.typeInfo &&
          backingStorage == other.backingStorage
      case _ => false
    }
  }
}

object System {
  implicit def system2map[ComponentType, T_IdTypes <: IdTypes](system: System[ComponentType, T_IdTypes]): mutable.Map[T_IdTypes#EntityId, ComponentType] = {
    system.backingStorage
  }
}
