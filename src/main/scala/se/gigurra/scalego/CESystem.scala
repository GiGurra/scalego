package se.gigurra.scalego

import scala.collection.mutable
import scala.language.implicitConversions

trait CESystem[T <: Component, ContextType] {
  protected def entries: mutable.Map[Entity.Id, T]

  def apply(entity: Entity.Id): T = entries.apply(entity)
  def get(entity: Entity.Id): Option[T] = entries.get(entity)
  def values: Iterable[T] = entries.values
  def keys: Iterable[Entity.Id] = entries.keys
  def size: Int = entries.size
  def isEmpty: Boolean = size == 0
  def nonEmpty: Boolean = !isEmpty

  def -=(entity: Entity.Id): Unit = entries -= entity
  def put(entity: Entity.Id, component: T, context: ContextType): Unit = entries.put(entity, component)
  def update(dt: Long, context: ContextType): Unit = {}// Executed every sim iteration

  def duplicate: CESystem[T, ContextType]
}

object CESystem {
  implicit def sys2Map[T <: Component](sys: CESystem[T, _]): scala.collection.Map[Entity.Id, T] = {
    sys.entries
  }
}
