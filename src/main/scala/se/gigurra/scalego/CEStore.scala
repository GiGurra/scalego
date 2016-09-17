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
  case class Builder[T_Types <: Types](systems: Map[T_Types#ComponentTypeId, CESystem[_, T_Types]] = Map.empty[T_Types#ComponentTypeId, CESystem[_, T_Types]]) {
    def +[T](system: CESystem[T, T_Types])(implicit typeInfo: ComponentTypeInfo[T, T_Types]): Builder[T_Types] = {
      Builder(systems + (implicitly[ComponentTypeInfo[T, T_Types]].id -> system.asInstanceOf[CESystem[_, T_Types]]))
    }
    def build: CEStore[T_Types] = new CEStore(systems)
  }
}
