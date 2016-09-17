package se.gigurra.scalego

import org.scalatest._
import org.scalatest.mock._

import scala.collection.mutable
import scala.language.postfixOps

class ECSSpec
  extends WordSpec
  with MockitoSugar
  with Matchers
  with OneInstancePerTest {

  class StringBasedIdTypes extends Types {
    override type ComponentTypeId = String
    override type EntityId = String
  }

  class LongBasedIdTypes extends Types {
    override type ComponentTypeId = Long
    override type EntityId = Long
  }

  case class Position(x: Int, y: Int)
  case class Velocity(x: Int, y: Int)

  "ECS" should {

    "Be created" in {
      noException should be thrownBy ECS()
    }

    "Add a system" in {
      val positionSystem = new System[(Int, Int), StringBasedIdTypes]("position")(mutable.HashMap())
      noException should be thrownBy ECS(positionSystem)
    }

    "Get the system of a component type" in {
      implicit val positionSystem = new System[Position, StringBasedIdTypes]("position")(mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringBasedIdTypes]("velocity")(mutable.HashMap())
      val store = ECS(positionSystem, velocitySystem)

      store.system[Position] shouldBe positionSystem
      store.system[Velocity] shouldBe velocitySystem
      positionSystem should not be velocitySystem
    }

    "Create entities in the store" in {
      implicit val positionSystem = new System[Position, StringBasedIdTypes]("position")(mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringBasedIdTypes]("velocity")(mutable.HashMap())

      val store = ECS(positionSystem, velocitySystem)

      val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      store.system[Position].size shouldBe 2
      store.system[Velocity].size shouldBe 2
      store.containsEntity("1") shouldBe true
      store.containsEntity("2") shouldBe true

      e1[Position] shouldBe Position(1,2)
      e1[Velocity] shouldBe Velocity(3,4)
      e2[Position] shouldBe Position(5,6)
      e2[Velocity] shouldBe Velocity(7,8)
    }

    "Get components of an entity" in {
      implicit val positionSystem = new System[Position, StringBasedIdTypes]("position")(mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringBasedIdTypes]("velocity")(mutable.HashMap())

      val store = ECS(positionSystem, velocitySystem)

      val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      store.componentsOf(e1).toSet shouldBe Set(Position(1, 2), Velocity(3, 4))
      store.componentsOf(e2).toSet shouldBe Set(Position(5, 6), Velocity(7, 8))
    }

    "Produce a debug info string of an entity" in {
      implicit val positionSystem = new System[Position, StringBasedIdTypes]("position")(mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringBasedIdTypes]("velocity")(mutable.HashMap())
      implicit val store = ECS(positionSystem, velocitySystem)

      val e = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      e.info shouldBe "Entity-1 { Position(1,2), Velocity(3,4) }"
    }

    "Delete entities from the entire store" in {
      implicit val positionSystem = new System[Position, StringBasedIdTypes]("position")(mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, StringBasedIdTypes]("velocity")(mutable.HashMap())

      val store = ECS(positionSystem, velocitySystem)

      val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      store -= "1"

      store.system[Position].size shouldBe 1
      store.system[Velocity].size shouldBe 1
      store.containsEntity("1") shouldBe false
      store.containsEntity("2") shouldBe true

      a[Entity.HasNoSuchComponent] should be thrownBy e1[Position]
      a[Entity.HasNoSuchComponent] should be thrownBy e1[Velocity]
      e2[Position] shouldBe Position(5,6)
      e2[Velocity] shouldBe Velocity(7,8)
    }

    "Support different types of entity and component type Ids" in {
      implicit val positionSystem = new System[Position, LongBasedIdTypes](1)(mutable.HashMap())
      implicit val velocitySystem = new System[Velocity, LongBasedIdTypes](2)(mutable.LongMap())

      val store = ECS(positionSystem, velocitySystem)

      val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = 1)
      val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = 2)

      store -= 1

      store.system[Position].size shouldBe 1
      store.system[Velocity].size shouldBe 1
      store.containsEntity(1) shouldBe false
      store.containsEntity(2) shouldBe true

      a[Entity.HasNoSuchComponent] should be thrownBy e1[Position]
      a[Entity.HasNoSuchComponent] should be thrownBy e1[Velocity]
      e2[Position] shouldBe Position(5,6)
      e2[Velocity] shouldBe Velocity(7,8)

    }
  }
}
