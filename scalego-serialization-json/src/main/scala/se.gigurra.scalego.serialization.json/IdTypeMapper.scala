package se.gigurra.scalego.serialization.json

/**
  * Created by johan on 2016-09-17.
  */
trait IdTypeMapper[SystemIdType, EntityIdType] {
  def intermediary2SystemId(id: String): SystemIdType
  def intermediary2EntityId(id: String): EntityIdType
  def compId2Intermediary(id: SystemIdType): String
  def entityId2Intermediary(id: EntityIdType): String
}

object IdTypeMapper {

  implicit val longIdMapper: IdTypeMapper[Long, Long] = new IdTypeMapper[Long, Long] {
    override def compId2Intermediary(id: Long): String = id.toString
    override def intermediary2SystemId(id: String): Long = id.toLong
    override def entityId2Intermediary(id: Long): String = id.toString
    override def intermediary2EntityId(id: String): Long = id.toLong
  }

  implicit val stringIdMapper: IdTypeMapper[String, String] = new IdTypeMapper[String, String] {
    override def compId2Intermediary(id: String): String = id
    override def intermediary2SystemId(id: String): String = id
    override def entityId2Intermediary(id: String): String = id
    override def intermediary2EntityId(id: String): String = id
  }
}

