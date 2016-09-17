package se.gigurra.scalego.core

import Entity.HasNoSuchComponent

import scala.collection.mutable
import scala.language.implicitConversions
import scala.language.existentials
import scala.reflect.ClassTag

/**
  * Created by johan on 2016-06-12.
  */
case class Entity[T_IdTypes <: IdTypes](id: T_IdTypes#EntityId) {
  def +=[ComponentType](component: ComponentType)(implicit system: System[ComponentType, T_IdTypes]): Entity[T_IdTypes] = {
    system.put(this.id, component)
    this
  }
  def get[ComponentType](implicit system: System[ComponentType, T_IdTypes]): Option[ComponentType] = system.get(this.id)
  def apply[ComponentType : ClassTag](implicit system: System[ComponentType, T_IdTypes]): ComponentType = system.getOrElse(this.id, throw HasNoSuchComponent(id, implicitly[ClassTag[ComponentType]].runtimeClass))
  def component[ComponentType : ClassTag](implicit system: System[ComponentType, T_IdTypes]): ComponentType = apply[ComponentType]
  def getComponent[ComponentType](implicit system: System[ComponentType, T_IdTypes]): Option[ComponentType] = get[ComponentType]
  def info(implicit ecs: ECS[T_IdTypes]): String = s"Entity-$id { ${ecs.componentsOf(this).mkString(", ")} }"
}

object Entity {

  object Builder {
    def +[ComponentType, T_IdTypes <: IdTypes](component: ComponentType)(implicit system: System[_ >: ComponentType, T_IdTypes]): EntityBuilder[T_IdTypes] = {
      EntityBuilder[T_IdTypes](Seq(UnAddedComponent(component, system)))
    }
  }

  case class EntityBuilder[T_IdTypes <: IdTypes](components: Seq[UnAddedComponent[_, T_IdTypes]] = new mutable.ArrayBuffer[UnAddedComponent[_, T_IdTypes]]) {
    def +[ComponentType](component: ComponentType)(implicit system: System[_ >: ComponentType, T_IdTypes]): EntityBuilder[T_IdTypes] = {
      EntityBuilder(components :+ UnAddedComponent(component, system))
    }
    def build(entityId: T_IdTypes#EntityId): Entity[T_IdTypes] = {
      components.foreach(_.addTo(entityId))
      Entity[T_IdTypes](entityId)
    }
  }

  case class HasNoSuchComponent(entityId: Any, componentType: Class[_])
    extends NoSuchElementException(s"Entity $entityId has no stored component of type ${componentType.getSimpleName}")

  case class UnAddedComponent[ComponentType, T_IdTypes <: IdTypes](component: ComponentType, system: System[ComponentType, T_IdTypes]){
    def addTo(entityId: T_IdTypes#EntityId): Unit = {
      system.put(entityId, component)
    }
    def removeFrom(entityId: T_IdTypes#EntityId): Unit = {
      system.remove(entityId)
    }
  }

  implicit def entity2Id[T_IdTypes <: IdTypes](entity: Entity[T_IdTypes]): T_IdTypes#EntityId = entity.id

}
