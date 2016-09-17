package se.gigurra.scalego.serialization

import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import org.scalatest.mock.MockitoSugar
import se.gigurra.scalego.core.{ECS, Entity, System}

import scala.collection.mutable
import scala.language.postfixOps
import ECSSerializerSpec._

class ECSSerializerSpec_write
  extends WordSpec
  with MockitoSugar
  with Matchers
  with OneInstancePerTest {

  object StringTestMapper extends TestMapper[StringBasedIdTypes] {
    override def intermediary2CompId(id: String): String = id
    override def intermediary2entityId(id: String): String = id
  }

  "ECSSerializer:write" should {

    "Create a serializable representation of an ECS" in {

      implicit val positionSystem = new System[Position, StringBasedIdTypes]("position", mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringBasedIdTypes]("velocity", mutable.HashMap())

      val ecs = ECS(positionSystem, velocitySystem)

      Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      implicit val formats = KnownSubtypes.empty

      val serializer = ECSSerializer(StringTestMapper)
      import serializer._

      val result = ecs.toSerializable

      result shouldBe
        SerializableEcs(List(
          SerializableSystem(systemId = "position", List(
            SerializableComponent(id = "2", TestIntermediateType(Position(5, 6)), subType = None),
            SerializableComponent(id = "1", TestIntermediateType(Position(1, 2)), subType = None)
          )),
          SerializableSystem(systemId = "velocity", List(
            SerializableComponent(id = "2", TestIntermediateType(Velocity(7, 8)), subType = None),
            SerializableComponent(id = "1", TestIntermediateType(Velocity(3, 4)), subType = None)
          ))
        ))

    }

    "Fail to a serializable representation of an ECS if systems contain unregistered/unknown subtypes" in {

      implicit val baseSystem = new System[BaseType, StringBasedIdTypes]("base-type", mutable.HashMap())

      val serializer = ECSSerializer(StringTestMapper)
      import serializer._
      val ecs = ECS(baseSystem)

      Entity.Builder + SubType(1, 2) build(entityId = "1")

      a[UnknownSubTypeForSerialization] should be thrownBy ecs.toSerializable
    }

    "Serializable representation of an ECS if systems contain registered/known subtypes" in {

      implicit val baseSystem = new System[BaseType, StringBasedIdTypes]("base-type", mutable.HashMap())

      val serializer = ECSSerializer(StringTestMapper, KnownSubtypes("cool-sub-type-id" -> classOf[SubType]))
      import serializer._
      val ecs = ECS(baseSystem)

      Entity.Builder + SubType(1, 2) build(entityId = "1")

      ecs.toSerializable shouldBe
        SerializableEcs(List(
          SerializableSystem(systemId = "base-type", Seq(
            SerializableComponent(id = "1", TestIntermediateType(SubType(1, 2)), subType = Some("cool-sub-type-id"))
          ))
        ))
    }

  }
}