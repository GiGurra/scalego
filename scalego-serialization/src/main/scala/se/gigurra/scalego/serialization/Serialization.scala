package se.gigurra.scalego.serialization

import se.gigurra.scalego.core.{ECS, System, Types}
import scala.reflect.runtime.universe.TypeTag

/**
  * Created by johan on 2016-09-17.
  */
object Serialization {

  implicit class SerializableECS[T_Types <: Types](ecs: ECS[T_Types]) {
    def toSerializableMap: Map[String, Any] = {
      ecs.systems.map { system =>

      }
      ???
    }
  }

  implicit class SerializableSystem[ComponentType : TypeTag, T_Types <: Types](ecs: System[ComponentType, T_Types]) {
    def toSerializableMap: Map[String, Any] = ???
  }


}
