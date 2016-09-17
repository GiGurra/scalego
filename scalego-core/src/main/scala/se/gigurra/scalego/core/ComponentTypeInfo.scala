package se.gigurra.scalego.core

import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
  * Created by johan on 2016-06-26.
  */
case class ComponentTypeInfo[ComponentType: ClassTag, T_Types <: Types](id: T_Types#SystemId) {
  def classTag: ClassTag[ComponentType] = implicitly[ClassTag[ComponentType]]
}

object ComponentTypeInfo {
  implicit def system2ComponentTypeInfo[ComponentType, T_Types <: Types](implicit system: System[ComponentType, T_Types]): ComponentTypeInfo[ComponentType, T_Types] = system.typeInfo
}
