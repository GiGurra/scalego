package se.gigurra.scalego

import org.scalatest._
import org.scalatest.mock._
import org.mockito.Mockito._
import org.mockito.Matchers._

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
      implicit val componentTypeInfo = ComponentTypeInfo[(Int, Int), StoreTypes]("position")
      val positionSystem = new CESystem(componentTypeInfo)
      noException should be thrownBy (CEStore.Builder[StoreTypes]() + positionSystem build : CEStore[StoreTypes])
    }


  }
}