package se.gigurra.scalego

import scala.language.implicitConversions

/**
  * Created by johan on 2016-06-12.
  */
case class Entity[T_Types <: Types](id: T_Types#EntityId) extends AnyVal {
  def +=[ComponentType](component: ComponentType)(implicit typeInfo: ComponentTypeInfo[ComponentType, T_Types], store: CEStore[T_Types]): Entity[T_Types] = {
    store.system[ComponentType].put(this.id, component)
    this
  }
  def get[ComponentType](implicit typeInfo: ComponentTypeInfo[ComponentType, T_Types], system: CESystem[ComponentType, T_Types]): Option[ComponentType] = system.get(this.id)
  def apply[ComponentType](implicit typeInfo: ComponentTypeInfo[ComponentType, T_Types], system: CESystem[ComponentType, T_Types]): ComponentType = system.apply(this.id)
  def component[ComponentType](implicit typeInfo: ComponentTypeInfo[ComponentType, T_Types], system: CESystem[ComponentType, T_Types]): ComponentType = apply[ComponentType]
  def getComponent[ComponentType](implicit typeInfo: ComponentTypeInfo[ComponentType, T_Types], system: CESystem[ComponentType, T_Types]): Option[ComponentType] = get[ComponentType]
}

object Entity {
  case class Builder[T_Types <: Types](entity: Entity[T_Types])(implicit store: CEStore[T_Types]) {
    def +[ComponentType](component: ComponentType)(implicit typeInfo: ComponentTypeInfo[ComponentType, T_Types]): Builder[T_Types] = {
      entity += component
      this
    }
    def build(): Entity[T_Types] = entity
  }
}
