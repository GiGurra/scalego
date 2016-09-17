package se.gigurra.scalego.core

import scala.language.implicitConversions
import scala.reflect.runtime.universe.TypeTag

/**
  * Created by johan on 2016-06-26.
  */
case class ComponentTypeInfo[ComponentType: TypeTag, T_Types <: Types](id: T_Types#ComponentTypeId) {
  def typeTag: TypeTag[ComponentType] = implicitly[TypeTag[ComponentType]]
}

object ComponentTypeInfo {
  implicit def system2ComponentTypeInfo[ComponentType, T_Types <: Types](implicit system: System[ComponentType, T_Types]): ComponentTypeInfo[ComponentType, T_Types] = system.typeInfo
}
