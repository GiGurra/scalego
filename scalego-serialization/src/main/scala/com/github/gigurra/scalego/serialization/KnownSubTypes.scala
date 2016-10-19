package com.github.gigurra.scalego.serialization

/**
  * Created by johan on 2016-09-17.
  */
case class KnownSubTypes(mappings: Set[(String, Class[_])]) {
  val id2Class: Map[String, Class[_]] = mappings.toMap
  val class2Id: Map[Class[_], String] = id2Class.map(_.swap)
  def +(newMappings: Set[(String, Class[_])]): KnownSubTypes = KnownSubTypes(mappings ++ newMappings)
  def +(newFormats: KnownSubTypes): KnownSubTypes = KnownSubTypes(mappings ++ newFormats.mappings)
  def +(newMapping: (String, Class[_])): KnownSubTypes = KnownSubTypes(mappings + newMapping)
}

object KnownSubTypes {
  def apply(mappings: (String, Class[_])*): KnownSubTypes = new KnownSubTypes(mappings.toSet)
  def fromShortClassName(types: Class[_]*): KnownSubTypes = apply(types.map(t => t.getSimpleName -> t): _*)
  def fromFullClassName(types: Class[_]*): KnownSubTypes = apply(types.map(t => t.getName -> t): _*)
  val empty: KnownSubTypes = apply()
}

