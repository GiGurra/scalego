package se.gigurra.scalego

import scala.reflect.ClassTag

/**
  * Created by johan on 2016-06-26.
  */
trait ComponentType[T <: Component] { def id: ComponentType.Id }
object ComponentType {
  def apply[T <: Component : ClassTag] = {
    new ComponentType[T] {
      def id: ComponentType.Id = implicitly[ClassTag[T]].runtimeClass.getSimpleName
    }
  }
  type Id = String
}
