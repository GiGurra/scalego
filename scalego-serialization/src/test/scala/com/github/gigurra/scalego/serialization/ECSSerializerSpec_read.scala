package com.github.gigurra.scalego.serialization

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import com.github.gigurra.scalego.core.{ECS, System}
import ECSSerializer._

import scala.collection.mutable
import scala.language.postfixOps

class ECSSerializerSpec_read
  extends WordSpec
    with MockitoSugar
    with Matchers
    with OneInstancePerTest {

    "ECSSerializer:read" should {

      "Append a serializable representation of an ECS into an existing ECS" in {

        implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
        implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())

        val ecs = ECS(positionSystem, velocitySystem)

        val serializer = ECSSerializer(TestMapper[StringIds]())
        import serializer._

        val serializedData = SerializableEcs(List(
          SerializableSystem(systemId = "position", List(
            SerializableComponent(id = "2", TestIntermediateType(Position(5, 6)), subType = None),
            SerializableComponent(id = "1", TestIntermediateType(Position(1, 2)), subType = None)
          )),
          SerializableSystem(systemId = "velocity", List(
            SerializableComponent(id = "2", TestIntermediateType(Velocity(7, 8)), subType = None),
            SerializableComponent(id = "1", TestIntermediateType(Velocity(3, 4)), subType = None)
          ))
        ))

        ecs.append(serializedData)

        ecs.system[Position].get("1") shouldBe Some(Position(1, 2))
        ecs.system[Position].get("2") shouldBe Some(Position(5, 6))

        ecs.system[Velocity].get("1") shouldBe Some(Velocity(3, 4))
        ecs.system[Velocity].get("2") shouldBe Some(Velocity(7, 8))

      }

      "Fail to append a serializable representation of an ECS into an existing ECS if reading unknown system Ids" in {
        val ecs = ECS(new System[Position, StringIds]("position", mutable.HashMap()))

        val serializer = ECSSerializer(TestMapper[StringIds]())
        import serializer._

        val serializedData = SerializableEcs(List(
          SerializableSystem(systemId = "position", List(
            SerializableComponent(id = "2", TestIntermediateType(Position(5, 6)), subType = None),
            SerializableComponent(id = "1", TestIntermediateType(Position(1, 2)), subType = None)
          )),
          SerializableSystem(systemId = "velocity", List(
            SerializableComponent(id = "2", TestIntermediateType(Velocity(7, 8)), subType = None),
            SerializableComponent(id = "1", TestIntermediateType(Velocity(3, 4)), subType = None)
          ))
        ))

        a[UnknownSystemForDeSerialization] should be thrownBy ecs.append(serializedData)

      }

      "Fail to append a serializable representation of an ECS into an existing ECS if reading unknown sub type Ids" in {
        val ecs = ECS(new System[BaseType, StringIds]("base-type", mutable.HashMap()))
        val serializer = ECSSerializer(TestMapper[StringIds]())
        import serializer._

        val serializedData = SerializableEcs(List(
          SerializableSystem(systemId = "base-type", Seq(
            SerializableComponent(id = "1", TestIntermediateType(SubType(1, 2)), subType = Some("cool-sub-type-id"))
          ))
        ))

        a[UnknownSubTypeForSerialization] should be thrownBy ecs.append(serializedData)
      }

      "Support sub types/class hierarchies if they are properly registered / Append a serializable representation of an ECS" in {
        implicit val system = new System[BaseType, StringIds]("base-type", mutable.HashMap())
        val ecs = ECS(system)
        val serializer = ECSSerializer(TestMapper[StringIds](), KnownSubTypes("cool-sub-type-id" -> classOf[SubType]))
        import serializer._

        val serializedData = SerializableEcs(List(
          SerializableSystem(systemId = "base-type", Seq(
            SerializableComponent(id = "1", TestIntermediateType(SubType(1, 2)), subType = Some("cool-sub-type-id"))
          ))
        ))

        ecs.append(serializedData)
        system.nonEmpty shouldBe true

        system.get("1") shouldBe Some(SubType(1,2))
      }
    }
  }
