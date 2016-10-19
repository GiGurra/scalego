package com.github.gigurra.scalego.serialization

/**
  * Created by johan on 2016-09-17.
  */
trait IdTypeMapper[IdType] {
  def intermediary2Id(id: String): IdType
  def id2Intermediary(id: IdType): String
}

object IdTypeMapper {

  implicit val longIdMapper: IdTypeMapper[Long] = new IdTypeMapper[Long] {
    def intermediary2Id(id: String): Long = id.toLong
    def id2Intermediary(id: Long): String = id.toString
  }

  implicit val stringIdMapper: IdTypeMapper[String] = new IdTypeMapper[String] {
    def intermediary2Id(id: String): String = id
    def id2Intermediary(id: String): String = id
  }
}
