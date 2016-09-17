package se.gigurra.scalego

import org.scalatest._
import org.scalatest.mock._

import scala.language.postfixOps

class CEStoreSpec
  extends WordSpec
  with MockitoSugar
  with Matchers
  with OneInstancePerTest {

  class StoreTypes extends Types {
    override type ComponentTypeId = String
    override type EntityId = String
    override type ProcessTime = Long
    override type ProcessContext = CEStore[this.type]
  }

  "CEStore" should {

    "Be created" in {
      noException should be thrownBy (CEStore.Builder[StoreTypes]() build : CEStore[StoreTypes])
    }

    "Add a system" in {
      val positionSystem = new CESystem(ComponentTypeInfo[(Int, Int), StoreTypes]("position"))
      noException should be thrownBy (CEStore.Builder[StoreTypes]() + positionSystem build : CEStore[StoreTypes])
    }

    "Get the system of a component type" in {
      case class Position(x: Int, y: Int)
      case class Velocity(x: Int, y: Int)
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StoreTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StoreTypes]("velocity")
      val positionSystem = new CESystem(positionTypeInfo)
      val velocitySystem = new CESystem(velocityTypeInfo)
      val store = CEStore.Builder[StoreTypes]() + positionSystem + velocitySystem build

      store.system[Position] shouldBe positionSystem
      store.system[Velocity] shouldBe velocitySystem
      positionSystem should not be velocitySystem
    }

    "Create entities in the store" in {
      case class Position(x: Int = 0, y: Int = 0)
      case class Velocity(x: Int = 0, y: Int = 0)
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StoreTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StoreTypes]("velocity")
      implicit val positionSystem = new CESystem(positionTypeInfo)
      implicit val velocitySystem = new CESystem(velocityTypeInfo)

      val store = CEStore.Builder[StoreTypes]() + positionSystem + velocitySystem build

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
      implicit val positionTypeInfo = ComponentTypeInfo[Position, StoreTypes]("position")
      implicit val velocityTypeInfo = ComponentTypeInfo[Velocity, StoreTypes]("velocity")
      implicit val positionSystem = new CESystem(positionTypeInfo)
      implicit val velocitySystem = new CESystem(velocityTypeInfo)

     val store = CEStore.Builder[StoreTypes]() + positionSystem + velocitySystem build

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
  }
}
