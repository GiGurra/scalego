package com.github.gigurra.scalego.serialization

import com.github.gigurra.scalego.core.IdTypes

/**
  * Created by johan on 2016-09-17.
  */
trait ObjectMapper[IntermediaryFormat, T_IdTypes <: IdTypes] {
  def obj2intermediary(obj: Any): IntermediaryFormat
  def intermediary2Obj(obj: IntermediaryFormat, cls: Class[_]): Any
}
