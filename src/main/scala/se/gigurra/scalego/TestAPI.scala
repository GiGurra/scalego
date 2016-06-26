package se.gigurra.scalego

/**
  * Created by johan on 2016-06-26.
  */
object TestAPI {
  implicit class TestableCEStore[ContextType](store: CEStore[ContextType]) {
    def allEntities: Set[Entity.Id] = {
      store.systems.values.flatMap(_.keys).toSet
    }
    def duplicate: CEStore[ContextType] = {
      store.copy(store.systems.map(p => p._1 -> p._2.duplicate))
    }
    def -(entity: Entity.Id): CEStore[ContextType] = {
      val out = duplicate
      out -= entity
      out
    }
  }
}
