package se.gigurra.scalego.serialization

import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import org.scalatest.mock.MockitoSugar
import se.gigurra.scalego.core.{ECS, Entity, System}

import scala.collection.mutable
import scala.language.postfixOps
import ECSSerializerSpec._
import org.json4s.Extraction._

class ECSSerializerSpec_write
  extends WordSpec
  with MockitoSugar
  with Matchers
  with OneInstancePerTest {

  "ECSSerializer:write" should {

    "Create a serializable representation of an ECS" in {

      implicit val positionSystem = new System[Position, StringBasedIdTypes]("position", mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringBasedIdTypes]("velocity", mutable.HashMap())

      val ecs = ECS(positionSystem, velocitySystem)

      Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      implicit val formats = SerializationFormats.empty

      val serializer = ECSSerializer(JsonTestMapper)
      import serializer._

      val result = ecs.toSerializable

      result shouldBe
        SerializableEcs(List(
          SerializableSystem(systemId = "position", List(
            SerializableComponent(id = "2", decompose(Map("x" -> 5, "y" -> 6)), subType = None),
            SerializableComponent(id = "1", decompose(Map("x" -> 1, "y" -> 2)), subType = None)
          )),
          SerializableSystem(systemId = "velocity", List(
            SerializableComponent(id = "2", decompose(Map("x" -> 7, "y" -> 8)), subType = None),
            SerializableComponent(id = "1", decompose(Map("x" -> 3, "y" -> 4)), subType = None)
          ))
        ))

    }

    "Fail to a serializable representation of an ECS if systems contain unregistered/unknown subtypes" in {

      implicit val baseSystem = new System[BaseType, StringBasedIdTypes]("base-type", mutable.HashMap())

      val serializer = ECSSerializer(JsonTestMapper)
      import serializer._
      val ecs = ECS(baseSystem)

      Entity.Builder + SubType(1, 2) build(entityId = "1")

      a[UnknownSubTypeForSerialization] should be thrownBy ecs.toSerializable
    }

    "Serializable representation of an ECS if systems contain registered/known subtypes" in {

      implicit val baseSystem = new System[BaseType, StringBasedIdTypes]("base-type", mutable.HashMap())

      val serializer = ECSSerializer(JsonTestMapper, SerializationFormats("cool-sub-type-id" -> classOf[SubType]))
      import serializer._
      val ecs = ECS(baseSystem)

      Entity.Builder + SubType(1, 2) build(entityId = "1")

      ecs.toSerializable shouldBe
        SerializableEcs(List(
          SerializableSystem(systemId = "base-type", Seq(
            SerializableComponent(id = "1", decompose(Map("x" -> 1, "y" -> 2)), subType = Some("cool-sub-type-id"))
          ))
        ))
    }

  }
}
