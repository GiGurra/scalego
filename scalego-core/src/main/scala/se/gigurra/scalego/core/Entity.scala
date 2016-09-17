package se.gigurra.scalego.core

import Entity.HasNoSuchComponent

import scala.collection.mutable
import scala.language.implicitConversions
import scala.language.existentials
import scala.reflect.ClassTag

/**
  * Created by johan on 2016-06-12.
  */
case class Entity[T_Types <: Types](id: T_Types#EntityId) {
  def +=[ComponentType](component: ComponentType)(implicit system: System[ComponentType, T_Types]): Entity[T_Types] = {
    system.put(this.id, component)
    this
  }
  def get[ComponentType](implicit system: System[ComponentType, T_Types]): Option[ComponentType] = system.get(this.id)
  def apply[ComponentType : ClassTag](implicit system: System[ComponentType, T_Types]): ComponentType = system.getOrElse(this.id, throw HasNoSuchComponent(id, implicitly[ClassTag[ComponentType]].runtimeClass))
  def component[ComponentType : ClassTag](implicit system: System[ComponentType, T_Types]): ComponentType = apply[ComponentType]
  def getComponent[ComponentType](implicit system: System[ComponentType, T_Types]): Option[ComponentType] = get[ComponentType]
  def info(implicit ecs: ECS[T_Types]): String = s"Entity-$id { ${ecs.componentsOf(this).mkString(", ")} }"
}

object Entity {

  object Builder {
    def +[ComponentType, T_Types <: Types](component: ComponentType)(implicit system: System[_ >: ComponentType, T_Types]): EntityBuilder[T_Types] = {
      EntityBuilder[T_Types](Seq(UnAddedComponent(component, system)))
    }
  }

  case class EntityBuilder[T_Types <: Types](components: Seq[UnAddedComponent[_, T_Types]] = new mutable.ArrayBuffer[UnAddedComponent[_, T_Types]]) {
    def +[ComponentType](component: ComponentType)(implicit system: System[_ >: ComponentType, T_Types]): EntityBuilder[T_Types] = {
      EntityBuilder(components :+ UnAddedComponent(component, system))
    }
    def build(entityId: T_Types#EntityId): Entity[T_Types] = {
      components.foreach(_.addTo(entityId))
      Entity[T_Types](entityId)
    }
  }

  case class HasNoSuchComponent(entityId: Any, componentType: Class[_])
    extends NoSuchElementException(s"Entity $entityId has no stored component of type ${componentType.getSimpleName}")

  case class UnAddedComponent[ComponentType, T_Types <: Types](component: ComponentType, system: System[ComponentType, T_Types]){
    def addTo(entityId: T_Types#EntityId): Unit = {
      system.put(entityId, component)
    }
    def removeFrom(entityId: T_Types#EntityId): Unit = {
      system.remove(entityId)
    }
  }

  implicit def entity2Id[T_Types <: Types](entity: Entity[T_Types]): T_Types#EntityId = entity.id

}
