package se.gigurra.scalego.serialization

import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import org.scalatest.mock.MockitoSugar
import se.gigurra.scalego.core.{ECS, Entity, System, Types}

import scala.collection.mutable
import scala.language.postfixOps
import Serialization._
import SerializationSpec._

class SerializationSpec
  extends WordSpec
  with MockitoSugar
  with Matchers
  with OneInstancePerTest {

  "Serialization" should {

    "Create a serializable representation of an ECS" in {
      case class Position(x: Int, y: Int)
      case class Velocity(x: Int, y: Int)

      implicit val positionSystem = new System[Position, StringBasedIdTypes]("position", mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringBasedIdTypes]("velocity", mutable.HashMap())

      val ecs = ECS(positionSystem, velocitySystem)

      Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      implicit val formats = SerializationFormats.empty

      val result = ecs.toSerializable

      result shouldBe
        SerializableEcs(List(
          SerializableSystem(List(
            SerializedComponent(id = "2", Position(5, 6), subType = None),
            SerializedComponent(id = "1", Position(1, 2), subType = None)
          ), "position"),
          SerializableSystem(List(
            SerializedComponent(id = "2", Velocity(7, 8), subType = None),
            SerializedComponent(id = "1", Velocity(3, 4), subType = None)
          ), "velocity")
        ))

    }

    "Fail to a serializable representation of an ECS if systems contain unregistered/unknown subtypes" in {

      trait BaseType
      case class SubType(x: Int, y: Int) extends BaseType

      implicit val baseSystem = new System[BaseType, StringBasedIdTypes]("base-type", mutable.HashMap())

      val ecs = ECS(baseSystem)

      Entity.Builder + SubType(1, 2) build(entityId = "1")

      implicit val formats = SerializationFormats.empty

      a[UnknownSubTypeForSerialization] should be thrownBy ecs.toSerializable
    }

  }
}

object SerializationSpec {

  class StringBasedIdTypes extends Types {
    override type ComponentTypeId = String
    override type EntityId = String
  }

  class LongBasedIdTypes extends Types {
    override type ComponentTypeId = Long
    override type EntityId = Long
  }

}