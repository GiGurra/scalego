package se.gigurra.scalego

import org.scalatest._
import org.scalatest.mock._

import scala.collection.mutable
import scala.language.postfixOps

class CEStoreSpec
  extends WordSpec
  with MockitoSugar
  with Matchers
  with OneInstancePerTest {

  class StringBasedIdTypes extends Types {
    override type ComponentTypeId = String
    override type EntityId = String
    override type ProcessInput = Long
  }

  class LongBasedIdTypes extends Types {
    override type ComponentTypeId = Long
    override type EntityId = Long
    override type ProcessInput = Long
  }

  case class Position(x: Int, y: Int)
  case class Velocity(x: Int, y: Int)

  "CEStore" should {

    "Be created" in {
      noException should be thrownBy CEStore()
    }

    "Add a system" in {
      val positionSystem = new CESystem(ComponentTypeInfo[(Int, Int), StringBasedIdTypes]("position"))(mutable.HashMap())
      noException should be thrownBy CEStore(positionSystem)
    }

    "Get the system of a component type" in {
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StringBasedIdTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StringBasedIdTypes]("velocity")
      val positionSystem = new CESystem(positionTypeInfo)(mutable.HashMap())
      val velocitySystem = new CESystem(velocityTypeInfo)(mutable.HashMap())
      val store = CEStore(positionSystem, velocitySystem)

      store.system[Position] shouldBe positionSystem
      store.system[Velocity] shouldBe velocitySystem
      positionSystem should not be velocitySystem
    }

    "Create entities in the store" in {
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StringBasedIdTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StringBasedIdTypes]("velocity")
      implicit val positionSystem = new CESystem(positionTypeInfo)(mutable.HashMap())
      implicit val velocitySystem = new CESystem(velocityTypeInfo)(mutable.HashMap())

      val store = CEStore(positionSystem, velocitySystem)

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
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StringBasedIdTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StringBasedIdTypes]("velocity")
      implicit val positionSystem = new CESystem(positionTypeInfo)(mutable.HashMap())
      implicit val velocitySystem = new CESystem(velocityTypeInfo)(mutable.HashMap())

      implicit val store = CEStore(positionSystem, velocitySystem)

      val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

      store.componentsOf(e1).toSet shouldBe Set(Position(1, 2), Velocity(3, 4))
      store.componentsOf(e2).toSet shouldBe Set(Position(5, 6), Velocity(7, 8))
    }

    "Produce a debug info string of an entity" in {
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StringBasedIdTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StringBasedIdTypes]("velocity")
      implicit val positionSystem = new CESystem(positionTypeInfo)(mutable.HashMap())
      implicit val velocitySystem = new CESystem(velocityTypeInfo)(mutable.HashMap())

      implicit val store = CEStore(positionSystem, velocitySystem)

      val e = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
      e.info shouldBe "Entity-1 { Position(1,2), Velocity(3,4) }"
    }

    "Delete entities from the entire store" in {
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StringBasedIdTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StringBasedIdTypes]("velocity")
      implicit val positionSystem = new CESystem(positionTypeInfo)(mutable.HashMap())
      implicit val velocitySystem = new CESystem(velocityTypeInfo)(mutable.HashMap())

      val store = CEStore(positionSystem, velocitySystem)

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
      implicit val positionTypeInfo = ComponentTypeInfo[Position, LongBasedIdTypes](1)
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, LongBasedIdTypes](2)
      implicit val positionSystem = new CESystem(positionTypeInfo)(mutable.HashMap())
      implicit val velocitySystem = new CESystem(velocityTypeInfo)(mutable.LongMap())

      val store = CEStore(positionSystem, velocitySystem)

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
