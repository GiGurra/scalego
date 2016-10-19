package com.github.gigurra.scalego.core

import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
  * Created by johan on 2016-06-26.
  */
case class ComponentTypeInfo[ComponentType: ClassTag, T_IdTypes <: IdTypes](id: T_IdTypes#SystemId) {
  def classTag: ClassTag[ComponentType] = implicitly[ClassTag[ComponentType]]
}

object ComponentTypeInfo {
  implicit def system2ComponentTypeInfo[ComponentType, T_IdTypes <: IdTypes](implicit system: System[ComponentType, T_IdTypes]): ComponentTypeInfo[ComponentType, T_IdTypes] = system.typeInfo
}
