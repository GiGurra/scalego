package com.github.gigurra.scalego.core

import scala.language.implicitConversions

/**
  * Created by johan on 2016-06-11.
  */
class ECS[T_IdTypes <: IdTypes] private(val systems: Map[T_IdTypes#SystemId, System[_, T_IdTypes]]) {

  def system[ComponentType](implicit typeInfo: ComponentTypeInfo[ComponentType, T_IdTypes]): System[ComponentType, T_IdTypes] = {
    systems.getOrElse(typeInfo.id, throw new RuntimeException(s"No system of type ${typeInfo.id} in $this")).asInstanceOf[System[ComponentType, T_IdTypes]]
  }

  def -=(entity: T_IdTypes#EntityId): Unit = {
    systems.values.foreach(_ -= entity)
  }

  def containsEntity(entity: T_IdTypes#EntityId): Boolean = {
    systems.values.exists(_.contains(entity))
  }

  def componentsOf(entity: T_IdTypes#EntityId): Seq[Any] = {
    systems.values.flatMap(_.get(entity)).toSeq
  }

  def clear(): Unit = {
    systems.values.foreach(_.clear())
  }

  def nonEmpty: Boolean = {
    systems.values.exists(_.nonEmpty)
  }

  def isEmpty: Boolean = {
    systems.values.forall(_.isEmpty)
  }

  override def hashCode(): Int = {
    systems.hashCode()
  }

  override def equals(other: Any): Boolean = {
    other match {
      case other : ECS[_] => systems == other.systems
      case _ => false
    }
  }

}

object ECS {
  def apply[T_IdTypes <: IdTypes](systems: System[_, T_IdTypes]*): ECS[T_IdTypes] = {
    new ECS[T_IdTypes](systems.map(system => system.typeInfo.id -> system).toMap)
  }
}
