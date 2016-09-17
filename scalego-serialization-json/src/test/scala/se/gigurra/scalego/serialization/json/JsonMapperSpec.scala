package se.gigurra.scalego.serialization.json

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import se.gigurra.scalego.core.{ECS, System}
import se.gigurra.scalego.serialization.ECSSerializerSpec._

import scala.collection.mutable
import scala.language.postfixOps
import org.json4s._
import org.json4s.jackson.JsonMethods._
import Extraction._
import JsonMapperSpec._

class JsonMapperSpec
  extends WordSpec
    with MockitoSugar
    with Matchers
    with OneInstancePerTest {

  val dut = new JsonMapper[StringBasedIdTypes]() {
    override def intermediary2CompId(id: String): StringBasedIdTypes#SystemId = id
    override def intermediary2entityId(id: String): StringBasedIdTypes#EntityId = id
  }
  implicit val fmts = dut.jsonFormats

  "JsonMapper" should {

    "Transform a case class into a JValue" in {
      dut.obj2intermediary(TestType(1, "2")) shouldBe decompose(Map("a" -> 1, "b" -> "2"))
    }

    "Transform a JValue into a case class" in {
      dut.intermediary2Obj(decompose(Map("a" -> 1, "b" -> "2")), classOf[TestType]) shouldBe TestType(1, "2")
    }

  }
}

object JsonMapperSpec {
  case class TestType(a: Int, b: String)
}
