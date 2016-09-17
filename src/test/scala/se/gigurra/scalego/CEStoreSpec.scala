package se.gigurra.scalego

import org.scalatest._
import org.scalatest.mock._

import scala.language.postfixOps

class CEStoreSpec
  extends WordSpec
  with MockitoSugar
  with Matchers
  with OneInstancePerTest {

  class StringBasedIdTypes extends Types {
    override type ComponentTypeId = String
    override type EntityId = String
    override type ProcessTime = Long
    override type ProcessContext = CEStore[StringBasedIdTypes]
  }

  class IntBasedIdTypes extends Types {
    override type ComponentTypeId = Int
    override type EntityId = Int
    override type ProcessTime = Long
    override type ProcessContext = CEStore[IntBasedIdTypes]
  }

  "CEStore" should {

    "Be created" in {
      noException should be thrownBy (CEStore.Builder[StringBasedIdTypes]() build : CEStore[StringBasedIdTypes])
    }

    "Add a system" in {
      val positionSystem = new CESystem(ComponentTypeInfo[(Int, Int), StringBasedIdTypes]("position"))
      noException should be thrownBy (CEStore.Builder[StringBasedIdTypes]() + positionSystem build : CEStore[StringBasedIdTypes])
    }

    "Get the system of a component type" in {
      case class Position(x: Int, y: Int)
      case class Velocity(x: Int, y: Int)
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StringBasedIdTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StringBasedIdTypes]("velocity")
      val positionSystem = new CESystem(positionTypeInfo)
      val velocitySystem = new CESystem(velocityTypeInfo)
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
      implicit val positionSystem = new CESystem(positionTypeInfo)
      implicit val velocitySystem = new CESystem(velocityTypeInfo)

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
      implicit val positionSystem = new CESystem(positionTypeInfo)
      implicit val velocitySystem = new CESystem(velocityTypeInfo)

      val store = CEStore.Builder[StringBasedIdTypes]() + positionSystem + velocitySystem build

      val e1 = store.entityBuilder(entityId = "1") + Position(1, 2) + Velocity(3, 4) build
      val e2 = store.entityBuilder(entityId = "2") + Position(5, 6) + Velocity(7, 8) build

      store -= "1"

      store.system[Position].size shouldBe 1
      store.system[Velocity].size shouldBe 1
      store.containsEntity("1") shouldBe false
      store.containsEntity("2") shouldBe true

      a[EntityHasNoSuchComponent] should be thrownBy e1[Position]
      a[EntityHasNoSuchComponent] should be thrownBy e1[Velocity]
      e2[Position] shouldBe Position(5,6)
      e2[Velocity] shouldBe Velocity(7,8)
    }

    "Support different types of entity and component type Ids" in {

      case class Position(x: Int = 0, y: Int = 0)
      case class Velocity(x: Int = 0, y: Int = 0)
      implicit val positionTypeInfo = ComponentTypeInfo[Position, IntBasedIdTypes](1)
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, IntBasedIdTypes](2)
      implicit val positionSystem = new CESystem(positionTypeInfo)
      implicit val velocitySystem = new CESystem(velocityTypeInfo)

      val store = CEStore.Builder[IntBasedIdTypes]() + positionSystem + velocitySystem build

      val e1 = store.entityBuilder(entityId = 1) + Position(1, 2) + Velocity(3, 4) build
      val e2 = store.entityBuilder(entityId = 2) + Position(5, 6) + Velocity(7, 8) build

      store -= 1

      store.system[Position].size shouldBe 1
      store.system[Velocity].size shouldBe 1
      store.containsEntity(1) shouldBe false
      store.containsEntity(2) shouldBe true

      a[EntityHasNoSuchComponent] should be thrownBy e1[Position]
      a[EntityHasNoSuchComponent] should be thrownBy e1[Velocity]
      e2[Position] shouldBe Position(5,6)
      e2[Velocity] shouldBe Velocity(7,8)

    }
  }
}
