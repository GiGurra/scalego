package se.gigurra.scalego

import scala.collection.mutable
import scala.language.implicitConversions

class CESystem[ComponentType, T_Types <: Types](val typeInfo: ComponentTypeInfo[ComponentType, T_Types]) extends mutable.HashMap[T_Types#EntityId, ComponentType] {
  def process(time: T_Types#ProcessTime, context: T_Types#ProcessContext): Unit = {}
}
