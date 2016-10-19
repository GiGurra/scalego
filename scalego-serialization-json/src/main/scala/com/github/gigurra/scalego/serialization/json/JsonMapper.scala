package com.github.gigurra.scalego.serialization.json

import org.json4s.Formats
import com.github.gigurra.scalego.serialization.ObjectMapper
import org.json4s.DefaultFormats
import org.json4s.Extraction._
import org.json4s.JsonAST.JValue
import com.github.gigurra.scalego.core.IdTypes

/**
  * Created by johan on 2016-09-17.
  */
class JsonMapper[T_IdTypes <: IdTypes](val jsonFormats: Formats = DefaultFormats) extends ObjectMapper[JValue, T_IdTypes] {
  override def obj2intermediary(obj: Any): JValue = decompose(obj)(jsonFormats)
  override def intermediary2Obj(intermediary: JValue, cls: Class[_]): Any = extract[Any](intermediary)(jsonFormats, Manifest.classType(cls))
}
