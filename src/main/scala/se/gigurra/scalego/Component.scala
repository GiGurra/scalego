package se.gigurra.scalego

/**
  * Created by johan on 2016-06-12.
  */
trait Component { def typ: ComponentType[_ <: Component] }
