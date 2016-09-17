package se.gigurra.scalego

import se.gigurra.scalego.Entity.HasNoSuchComponent

import scala.collection.mutable
import scala.language.implicitConversions
import scala.language.existentials
import scala.reflect.ClassTag

/**
  * Created by johan on 2016-06-12.
  */
case class Entity[T_Types <: Types](id: T_Types#EntityId) {
  def +=[ComponentType](component: ComponentType)(implicit system: CESystem[ComponentType, T_Types]): Entity[T_Types] = {
    system.put(this.id, component)
    this
  }
  def get[ComponentType](implicit system: CESystem[ComponentType, T_Types]): Option[ComponentType] = system.get(this.id)
  def apply[ComponentType : ClassTag](implicit system: CESystem[ComponentType, T_Types]): ComponentType = system.getOrElse(this.id, throw HasNoSuchComponent(id, implicitly[ClassTag[ComponentType]].runtimeClass))
  def component[ComponentType : ClassTag](implicit system: CESystem[ComponentType, T_Types]): ComponentType = apply[ComponentType]
  def getComponent[ComponentType](implicit system: CESystem[ComponentType, T_Types]): Option[ComponentType] = get[ComponentType]
}

object Entity {
  case class Builder[T_Types <: Types](entityId: T_Types#EntityId, components: mutable.ArrayBuffer[UnAddedComponent[_, T_Types]] = new mutable.ArrayBuffer[UnAddedComponent[_, T_Types]])(implicit store: CEStore[T_Types]) {
    def +[ComponentType](component: ComponentType)(implicit system: CESystem[ComponentType, T_Types]): Builder[T_Types] = {
      components += UnAddedComponent(entityId, component)
      this
    }
    def build: Entity[T_Types] = {
      components.foreach(_.addTo(store))
      Entity[T_Types](entityId)
    }
  }

  case class UnAddedComponent[ComponentType, T_Types <: Types](entityId: T_Types#EntityId,
                                                               component: ComponentType)
                                                              (implicit system: CESystem[ComponentType, T_Types]){
    def addTo(store: CEStore[T_Types]): Unit = {
      system.put(entityId, component)
    }
  }

  case class HasNoSuchComponent(entityId: Any, componentType: Class[_])
    extends NoSuchElementException(s"Entity $entityId has no stored component of type ${componentType.getSimpleName}")
}
