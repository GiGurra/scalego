package se.gigurra.scalego

import scala.language.implicitConversions

/**
  * Created by johan on 2016-06-11.
  */
case class CEStore[ContextType](systems: Map[ComponentType.Id, CESystem[Component, ContextType]])  {

  def system[T <: Component : ComponentType]: CESystem[T, ContextType] = {
    val typ = implicitly[ComponentType[T]]
    systems.getOrElse(typ.id, throw new RuntimeException(s"No system of type $typ in $this")).asInstanceOf[CESystem[T, ContextType]]
  }

  def -=(entity: Entity.Id): Unit = {
    systems.values.foreach(_ -= entity)
  }

  def componentsOf(entity: Entity.Id): Iterable[Component] = {
    for {
      system <- systems.values
      component <- system.get(entity)
    } yield {
      component
    }
  }

  def containsEntity(entity: Entity.Id): Boolean = {
    systems.values.exists(_.contains(entity))
  }

}

object CEStore {

  def newBuilder[ContextType]: Builder[ContextType] = Builder(Map.empty)

  case class Builder[ContextType](systems: Map[ComponentType.Id, CESystem[Component, ContextType]]) {
    def ++[T <: Component : ComponentType](system: CESystem[T, ContextType]): Builder[ContextType] = {
      Builder(systems + (implicitly[ComponentType[T]].id -> system.asInstanceOf[CESystem[Component, ContextType]]))
    }
    def build: CEStore[ContextType] = CEStore(systems)
  }

  implicit def store2map(store: CEStore[_]): scala.collection.Map[ComponentType.Id, CESystem[Component, _]] = store.systems
}
