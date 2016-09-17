package se.gigurra.scalego
import scala.language.implicitConversions

/**
  * Created by johan on 2016-06-26.
  */
case class ComponentTypeInfo[ComponentType, T_Types <: Types](id: T_Types#ComponentTypeId)

object ComponentTypeInfo {
  implicit def system2ComponentTypeInfo[ComponentType, T_Types <: Types](implicit system: CESystem[ComponentType, T_Types]): ComponentTypeInfo[ComponentType, T_Types] = system.typeInfo
}
