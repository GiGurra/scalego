package com.github.gigurra.scalego.serialization.json

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import com.github.gigurra.scalego.core.{ECS, Entity, System}

import scala.collection.mutable
import scala.language.postfixOps
import org.json4s._
import org.json4s.jackson.JsonMethods._

class JsonSerializerSpec
  extends WordSpec
    with MockitoSugar
    with Matchers
    with OneInstancePerTest {


  "JsonSerializer" should {

    "when using String based Ids" should {

      "Transform ecs into JSON" in {

        val dut = JsonSerializer[StringIds]()
        import dut._

        implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
        implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())

        val ecs = ECS(positionSystem, velocitySystem)

        Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
        Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

        val ugly = ecs.toJson(pretty = false)
        val pretty = ecs.toJson(pretty = true)

        ugly shouldBe "{\"systems\":[{\"systemId\":\"position\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":5,\"y\":6}},{\"id\":\"1\",\"data\":{\"x\":1,\"y\":2}}]},{\"systemId\":\"velocity\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":7,\"y\":8}},{\"id\":\"1\",\"data\":{\"x\":3,\"y\":4}}]}]}"

        parse(ugly) shouldBe parse(pretty)

      }

      "Append json to ecs" in {

        val dut = JsonSerializer[StringIds]()
        import dut._

        implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
        implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())

        val ecs = ECS(positionSystem, velocitySystem)

        val json = "{\"systems\":[{\"systemId\":\"position\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":5,\"y\":6}},{\"id\":\"1\",\"data\":{\"x\":1,\"y\":2}}]},{\"systemId\":\"velocity\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":7,\"y\":8}},{\"id\":\"1\",\"data\":{\"x\":3,\"y\":4}}]}]}"
        ecs.appendJson(json)

        ecs.system[Position].get("1") shouldBe Some(Position(1,2))
        ecs.system[Position].get("2") shouldBe Some(Position(5,6))

        ecs.system[Velocity].get("1") shouldBe Some(Velocity(3,4))
        ecs.system[Velocity].get("2") shouldBe Some(Velocity(7,8))

      }
    }

    "when using Long based Ids" should {

      "Transform ecs into JSON" in {

        val dut = JsonSerializer[LongIds]()
        import dut._

        implicit val positionSystem = new System[Position, LongIds](4, mutable.LongMap())
        implicit val velocitySystem = new System[Velocity, LongIds](5, mutable.LongMap())

        val ecs = ECS(positionSystem, velocitySystem)

        Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = 1)
        Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = 2)

        val ugly = ecs.toJson(pretty = false)
        val pretty = ecs.toJson(pretty = true)

        ugly shouldBe "{\"systems\":[{\"systemId\":\"4\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":5,\"y\":6}},{\"id\":\"1\",\"data\":{\"x\":1,\"y\":2}}]},{\"systemId\":\"5\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":7,\"y\":8}},{\"id\":\"1\",\"data\":{\"x\":3,\"y\":4}}]}]}"

        parse(ugly) shouldBe parse(pretty)

      }

      "Append json to ecs" in {

        val dut = JsonSerializer[LongIds]()
        import dut._

        implicit val positionSystem = new System[Position, LongIds](4, mutable.LongMap())
        implicit val velocitySystem = new System[Velocity, LongIds](5, mutable.LongMap())

        val ecs = ECS(positionSystem, velocitySystem)

        val json = "{\"systems\":[{\"systemId\":\"4\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":5,\"y\":6}},{\"id\":\"1\",\"data\":{\"x\":1,\"y\":2}}]},{\"systemId\":\"5\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":7,\"y\":8}},{\"id\":\"1\",\"data\":{\"x\":3,\"y\":4}}]}]}"
        ecs.appendJson(json)

        ecs.system[Position].get(1) shouldBe Some(Position(1,2))
        ecs.system[Position].get(2) shouldBe Some(Position(5,6))

        ecs.system[Velocity].get(1) shouldBe Some(Velocity(3,4))
        ecs.system[Velocity].get(2) shouldBe Some(Velocity(7,8))
      }
    }


  }
}

object JsonSerializerSpec {
  case class TestType(a: Int, b: String)
}
