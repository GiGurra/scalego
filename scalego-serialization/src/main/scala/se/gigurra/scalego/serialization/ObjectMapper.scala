package se.gigurra.scalego.serialization

import se.gigurra.scalego.core.Types

/**
  * Created by johan on 2016-09-17.
  */
trait ObjectMapper[IntermediaryFormat, T_Types <: Types] {
  def obj2intermediary(obj: Any): IntermediaryFormat
  def intermediary2Obj(obj: IntermediaryFormat, cls: Class[_]): Any
}
