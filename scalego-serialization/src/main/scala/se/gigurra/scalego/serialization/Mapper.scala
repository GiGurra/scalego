package se.gigurra.scalego.serialization

import se.gigurra.scalego.core.Types

/**
  * Created by johan on 2016-09-17.
  */
trait Mapper[IntermediaryFormat, T_Types <: Types] {
  def obj2intermediary(obj: Any): IntermediaryFormat
  def intermediary2Obj(obj: IntermediaryFormat, cls: Class[_]): Any
  def systemId2Intermediary(id: T_Types#SystemId): String
  def intermediary2SystemId(id: String): T_Types#SystemId
  def entityId2Intermediary(id: T_Types#EntityId): String
  def intermediary2EntityId(id: String): T_Types#EntityId
}
