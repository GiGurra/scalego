package se.gigurra.scalego.serialization

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import se.gigurra.scalego.core.{ECS, Entity, System}
import se.gigurra.scalego.serialization.SerializationSpec._
import org.json4s.Extraction._

import scala.collection.mutable
import scala.language.postfixOps

class SerializationSpec_read
  extends WordSpec
    with MockitoSugar
    with Matchers
    with OneInstancePerTest {

    "Serialization" should {

      "Append a serializable representation of an ECS into an existing ECS" in {

        implicit val positionSystem = new System[Position, StringBasedIdTypes]("position", mutable.HashMap())
        implicit val velocitySystem = new System[Velocity, StringBasedIdTypes]("velocity", mutable.HashMap())

        val ecs = ECS(positionSystem, velocitySystem)

        val serializer = ECSSerializer(JsonTestMapper)
        import serializer._

        val serializedData = SerializableEcs(List(
          SerializableSystem(systemId = "position", List(
            SerializableComponent(id = "2", decompose(Map("x" -> 5, "y" -> 6)), subType = None),
            SerializableComponent(id = "1", decompose(Map("x" -> 1, "y" -> 2)), subType = None)
          )),
          SerializableSystem(systemId = "velocity", List(
            SerializableComponent(id = "2", decompose(Map("x" -> 7, "y" -> 8)), subType = None),
            SerializableComponent(id = "1", decompose(Map("x" -> 3, "y" -> 4)), subType = None)
          ))
        ))

        implicit val formats = SerializationFormats.empty
        ecs.append(serializedData)

        ecs.system[Position].get("1") shouldBe Some(Position(1, 2))
        ecs.system[Position].get("2") shouldBe Some(Position(5, 6))

        ecs.system[Velocity].get("1") shouldBe Some(Velocity(3, 4))
        ecs.system[Velocity].get("2") shouldBe Some(Velocity(7, 8))

      }

    }
  }
