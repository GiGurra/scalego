package se.gigurra.scalego

/**
  * Created by johan on 2016-06-26.
  */
case class ComponentTypeInfo[T, T_Types <: Types](id: T_Types#ComponentTypeId) {
  type ComponentType = T
}
