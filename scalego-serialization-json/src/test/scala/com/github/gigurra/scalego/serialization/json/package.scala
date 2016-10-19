package com.github.gigurra.scalego.serialization

import com.github.gigurra.scalego.core.IdTypes

/**
  * Created by johan on 2016-09-17.
  */
package object json {

  trait BaseType
  case class SubType(x: Int, y: Int) extends BaseType

  case class Position(x: Int, y: Int)
  case class Velocity(x: Int, y: Int)

  type StringIds = IdTypes {
    type SystemId = String
    type EntityId = String
  }

  type LongIds = IdTypes {
    type SystemId = Long
    type EntityId = Long
  }

  case class TestIntermediateType(obj: Any)
  case class TestMapper[T_IdTypes <: IdTypes]() extends ObjectMapper[TestIntermediateType, T_IdTypes] {
    def obj2intermediary(obj: Any): TestIntermediateType = TestIntermediateType(obj)
    def intermediary2Obj(intermediary: TestIntermediateType, cls: Class[_]): Any = intermediary.obj
  }
}
