package com.github.gigurra.scalego.core

import org.scalatest._
import org.scalatest.mock._

import scala.collection.mutable
import scala.language.postfixOps

class ECSSpec
  extends WordSpec
  with MockitoSugar
  with Matchers
  with OneInstancePerTest {

  type StringIds = IdTypes {
    type SystemId = String
    type EntityId = String
  }

  type LongIds = IdTypes {
    type SystemId = Long
    type EntityId = Long
  }

  case class Position(x: Int, y: Int)
  case class Velocity(x: Int, y: Int)

  "ECS" should {

    "Be created" in {
      noException should be thrownBy ECS()
    }

    "Add a system" in {
      val positionSystem = new System[(Int, Int), StringIds]("position", mutable.HashMap())
      noException should be thrownBy ECS(positionSystem)
    }

    "Get the system of a component type" in {
      implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())
      val ecs = ECS(positionSystem, velocitySystem)

      ecs.system[Position] shouldBe positionSystem
      ecs.system[Velocity] shouldBe velocitySystem
      positionSystem should not be velocitySystem
    }

    "Create entities in the ecs" in {
      implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())

      val ecs = ECS(positionSystem, velocitySystem)

      val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      ecs.system[Position].size shouldBe 2
      ecs.system[Velocity].size shouldBe 2
      ecs.containsEntity("1") shouldBe true
      ecs.containsEntity("2") shouldBe true

      e1[Position] shouldBe Position(1,2)
      e1[Velocity] shouldBe Velocity(3,4)
      e2[Position] shouldBe Position(5,6)
      e2[Velocity] shouldBe Velocity(7,8)
    }

    "Have isEmpty and nonEmpty methods" in {
      implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())

      val ecs = ECS(positionSystem, velocitySystem)

      ecs.nonEmpty shouldBe false
      ecs.isEmpty shouldBe true

      val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      ecs.nonEmpty shouldBe true
      ecs.isEmpty shouldBe false
    }

    "Get components of an entity" in {
      implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())

      val ecs = ECS(positionSystem, velocitySystem)

      val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      ecs.componentsOf(e1).toSet shouldBe Set(Position(1, 2), Velocity(3, 4))
      ecs.componentsOf(e2).toSet shouldBe Set(Position(5, 6), Velocity(7, 8))
    }

    "Produce a debug info string of an entity" in {
      implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())
      implicit val ecs = ECS(positionSystem, velocitySystem)

      val e = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      e.info shouldBe "Entity-1 { Position(1,2), Velocity(3,4) }"
    }

    "Delete entities from the entire ecs" in {
      implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())

      val ecs = ECS(positionSystem, velocitySystem)

      val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      ecs -= "1"

      ecs.system[Position].size shouldBe 1
      ecs.system[Velocity].size shouldBe 1
      ecs.containsEntity("1") shouldBe false
      ecs.containsEntity("2") shouldBe true

      a[Entity.HasNoSuchComponent] should be thrownBy e1[Position]
      a[Entity.HasNoSuchComponent] should be thrownBy e1[Velocity]
      e2[Position] shouldBe Position(5,6)
      e2[Velocity] shouldBe Velocity(7,8)
    }

    "Support different types of entity and component type Ids" in {
      implicit val positionSystem = new System[Position, LongIds](1, mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, LongIds](2, mutable.LongMap())

      val ecs = ECS(positionSystem, velocitySystem)

      val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = 1)
      val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = 2)

      ecs -= 1

      ecs.system[Position].size shouldBe 1
      ecs.system[Velocity].size shouldBe 1
      ecs.containsEntity(1) shouldBe false
      ecs.containsEntity(2) shouldBe true

      a[Entity.HasNoSuchComponent] should be thrownBy e1[Position]
      a[Entity.HasNoSuchComponent] should be thrownBy e1[Velocity]
      e2[Position] shouldBe Position(5,6)
      e2[Velocity] shouldBe Velocity(7,8)

    }
  }
}
