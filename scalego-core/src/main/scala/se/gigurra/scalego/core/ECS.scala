package se.gigurra.scalego.core

import scala.language.implicitConversions

/**
  * Created by johan on 2016-06-11.
  */
class ECS[T_Types <: Types] private(val systems: Map[T_Types#ComponentTypeId, System[_, T_Types]]) {

  def system[T](implicit typeInfo: ComponentTypeInfo[T, T_Types]): System[T, T_Types] = {
    val typ = implicitly[ComponentTypeInfo[T, T_Types]]
    systems.getOrElse(typ.id, throw new RuntimeException(s"No system of type $typ in $this")).asInstanceOf[System[T, T_Types]]
  }

  def -=(entity: T_Types#EntityId): Unit = {
    systems.values.foreach(_ -= entity)
  }

  def containsEntity(entity: T_Types#EntityId): Boolean = {
    systems.values.exists(_.contains(entity))
  }

  def componentsOf(entity: T_Types#EntityId): Seq[Any] = {
    systems.values.flatMap(_.get(entity)).toSeq
  }

  def clear(): Unit = {
    systems.values.foreach(_.clear())
  }
}

object ECS {
  def apply[T_Types <: Types](systems: System[_, T_Types]*): ECS[T_Types] = {
    new ECS[T_Types](systems.map(system => system.typeInfo.id -> system).toMap)
  }
}
