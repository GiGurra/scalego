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

  "CEStore" should {

    "Be created" in {
      noException should be thrownBy (CEStore.Builder[StringBasedIdTypes]() build : CEStore[StringBasedIdTypes])
    }

    "Add a system" in {
      val positionSystem = new CESystem(ComponentTypeInfo[(Int, Int), StringBasedIdTypes]("position"))(mutable.HashMap())
      noException should be thrownBy (CEStore.Builder[StringBasedIdTypes]() + positionSystem build : CEStore[StringBasedIdTypes])
    }

    "Get the system of a component type" in {
      case class Position(x: Int, y: Int)
      case class Velocity(x: Int, y: Int)
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StringBasedIdTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StringBasedIdTypes]("velocity")
      val positionSystem = new CESystem(positionTypeInfo)(mutable.HashMap())
      val velocitySystem = new CESystem(velocityTypeInfo)(mutable.HashMap())
      val store = CEStore.Builder[StringBasedIdTypes]() + positionSystem + velocitySystem build

      store.system[Position] shouldBe positionSystem
      store.system[Velocity] shouldBe velocitySystem
      positionSystem should not be velocitySystem
    }

    "Create entities in the store" in {
      case class Position(x: Int = 0, y: Int = 0)
      case class Velocity(x: Int = 0, y: Int = 0)
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StringBasedIdTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StringBasedIdTypes]("velocity")
      implicit val positionSystem = new CESystem(positionTypeInfo)(mutable.HashMap())
      implicit val velocitySystem = new CESystem(velocityTypeInfo)(mutable.HashMap())

      val store = CEStore.Builder[StringBasedIdTypes]() + positionSystem + velocitySystem build

      val e1 = store.entityBuilder(entityId = "1") + Position(1, 2) + Velocity(3, 4) build
      val e2 = store.entityBuilder(entityId = "2") + Position(5, 6) + Velocity(7, 8) build

      store.system[Position].size shouldBe 2
      store.system[Velocity].size shouldBe 2
      store.containsEntity("1") shouldBe true
      store.containsEntity("2") shouldBe true

      e1[Position] shouldBe Position(1,2)
      e1[Velocity] shouldBe Velocity(3,4)
      e2[Position] shouldBe Position(5,6)
      e2[Velocity] shouldBe Velocity(7,8)
    }

    "Delete entities from the entire store" in {
      case class Position(x: Int = 0, y: Int = 0)
      case class Velocity(x: Int = 0, y: Int = 0)
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StringBasedIdTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StringBasedIdTypes]("velocity")
      implicit val positionSystem = new CESystem(positionTypeInfo)(mutable.HashMap())
      implicit val velocitySystem = new CESystem(velocityTypeInfo)(mutable.HashMap())

      val store = CEStore.Builder[StringBasedIdTypes]() + positionSystem + velocitySystem build

      val e1 = store.entityBuilder(entityId = "1") + Position(1, 2) + Velocity(3, 4) build
      val e2 = store.entityBuilder(entityId = "2") + Position(5, 6) + Velocity(7, 8) build

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

      case class Position(x: Int = 0, y: Int = 0)
      case class Velocity(x: Int = 0, y: Int = 0)
      implicit val positionTypeInfo = ComponentTypeInfo[Position, LongBasedIdTypes](1)
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, LongBasedIdTypes](2)
      implicit val positionSystem = new CESystem(positionTypeInfo)(mutable.HashMap())
      implicit val velocitySystem = new CESystem(velocityTypeInfo)(mutable.LongMap())

      val store = CEStore.Builder[LongBasedIdTypes]() + positionSystem + velocitySystem build

      val e1 = store.entityBuilder(entityId = 1) + Position(1, 2) + Velocity(3, 4) build
      val e2 = store.entityBuilder(entityId = 2) + Position(5, 6) + Velocity(7, 8) build

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
