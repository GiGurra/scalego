package se.gigurra.scalego

import se.gigurra.scalego.core.Types

/**
  * Created by johan on 2016-09-17.
  */
package object serialization {

  trait BaseType
  case class SubType(x: Int, y: Int) extends BaseType

  case class Position(x: Int, y: Int)
  case class Velocity(x: Int, y: Int)

  class StringBasedIdTypes extends Types {
    override type SystemId = String
    override type EntityId = String
  }

  class LongBasedIdTypes extends Types {
    override type SystemId = Long
    override type EntityId = Long
  }

  case class TestIntermediateType(obj: Any)
  case class TestMapper[T_Types <: Types]() extends ObjectMapper[TestIntermediateType, T_Types] {
    def obj2intermediary(obj: Any): TestIntermediateType = TestIntermediateType(obj)
    def intermediary2Obj(intermediary: TestIntermediateType, cls: Class[_]): Any = intermediary.obj
  }
}
