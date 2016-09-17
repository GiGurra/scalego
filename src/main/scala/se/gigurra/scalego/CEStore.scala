package se.gigurra.scalego

import scala.language.implicitConversions

/**
  * Created by johan on 2016-06-11.
  */
class CEStore[T_Types <: Types] private(systems: Map[T_Types#ComponentTypeId, CESystem[_, T_Types]]) {

  def system[T](implicit typeInfo: ComponentTypeInfo[T, T_Types]): CESystem[T, T_Types] = {
    val typ = implicitly[ComponentTypeInfo[T, T_Types]]
    systems.getOrElse(typ.id, throw new RuntimeException(s"No system of type $typ in $this")).asInstanceOf[CESystem[T, T_Types]]
  }

  def -=(entity: T_Types#EntityId): Unit = {
    systems.values.foreach(_ -= entity)
  }

  def containsEntity(entity: T_Types#EntityId): Boolean = {
    systems.values.exists(_.contains(entity))
  }
}

object CEStore {
  def apply[T_Types <: Types](systems: CESystem[_, T_Types]*): CEStore[T_Types] = {
    new CEStore[T_Types](systems.map(system => system.typeInfo.id -> system).toMap)
  }
}
