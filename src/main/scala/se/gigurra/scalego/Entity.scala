package se.gigurra.scalego

import scala.language.implicitConversions

/**
  * Created by johan on 2016-06-12.
  */
case class Entity(id: Entity.Id) extends AnyVal {
  def +=[T <: Component: ComponentType, ContextType](component: T)(implicit store: CEStore[ContextType], context: ContextType): Entity = {
    store.system[T].put(this, component, context)
    this
  }
  def get[T <: Component: ComponentType](implicit system: CESystem[T, _]): Option[T] = system.get(this)
  def apply[T <: Component: ComponentType](implicit system: CESystem[T, _]): T = system.apply(this)
  def component[T <: Component: ComponentType](implicit system: CESystem[T, _]): T = apply[T]
  def getComponent[T <: Component: ComponentType](implicit system: CESystem[T, _]): Option[T] = get[T]
  def components(implicit store: CEStore[_]): Iterable[Component] = store.componentsOf(this)
}

object Entity {
  type Id = String

  def builder[ContextType](id: Entity.Id)(implicit store: CEStore[ContextType], context: ContextType): Builder[ContextType] =
    new Builder[ContextType](Entity(id))(store, context)

  case class Builder[ContextType](entity: Entity)(implicit store: CEStore[ContextType], context: ContextType) {
    def +[T <: Component: ComponentType](component: T): Builder[ContextType] = {
      entity += component
      this
    }
  }
  object Builder {
    implicit def builder2entity(b: Builder[_]): Entity = b.entity
  }

  implicit def e2id(e: Entity): Entity.Id = e.id
}
