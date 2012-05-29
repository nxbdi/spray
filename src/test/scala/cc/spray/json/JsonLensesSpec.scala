package cc.spray.json

import DefaultJsonProtocol._

import org.specs2.mutable.Specification

class JsonLensesSpec extends Specification {
  val json = JsonParser(
    """{
      |  "n": 2,
      |  "els": [
      |     {
      |       "name": "John",
      |       "money": 23
      |     },
      |     {
      |       "name": "Paul",
      |       "money": 42
      |     }
      |  ]
      |}
    """.stripMargin)

  import JsonLenses._

  val n = field("n")

  "Lenses" should {
    "access" in {
      "field" in {
        json extract "n".get[Int] must be_==(2)
      }
      "field of member" in {
        val json = JsonParser("""{"n": {"b": 4}}""")

        json extract ("n" / "b").get[Int] must be_==(4)
      }
      "element of array" in {
        val json = JsonParser("""["a", "b", 2, 5, 8, 3]""")

        json extract element(3).get[Int] must be_==(5)
      }
      "finding an element" in {
        "in a homogenous array" in {
          val json = JsonParser("""[18, 23, 2, 5, 8, 3]""")

          "if type matches" in {
            json extract JsonLenses.find(JsonLenses.value.is[Int](_ < 4)).get[Int] must beSome(2)
          }
          "if type is wrong" in {
            json extract JsonLenses.find(JsonLenses.value.is[String](_ < "unknown")).get[Int] must beNone
          }
        }
        "in an imhomogenous array" in {
          val json = JsonParser("""["a", "b", 2, 5, 8, 3]""")

          json extract JsonLenses.find(JsonLenses.value.is[Int](_ < 4)).get[Int] must beSome(2)
          json extract JsonLenses.find(JsonLenses.value.is[String](_ < "unknown")).get[Int] must beNone
        }
      }
    }

    "modify" in {
      "set field" in {
        val simple = JsonParser( """{"n": 12}""")

        simple.update(n ! set(23)) must be_json( """{"n": 23}""")
      }
      "update field" in {
        val simple = JsonParser( """{"n": 12}""")
        simple.update(n ! updated[Int](_ + 1)) must be_json( """{"n": 13}""")
      }
      "set field of member" in {
        val json = JsonParser( """{"n": {"b": 4}}""")

        json update ("n" / "b" ! set(23)) must be_json( """{"n": {"b": 23}}""")
      }
      "update field of member" in {
        val json = JsonParser( """{"n": {"b": 4}}""")

        json update ("n" / "b" ! updated[Int](1 +)) must be_json( """{"n": {"b": 5}}""")
      }
      "set element of array" in {
        val json = JsonParser("""["a", "b", 2, 5, 8, 3]""")

        json update (element(3) ! set(35)) must be_json("""["a", "b", 2, 35, 8, 3]""")
      }
      "change a found element" in {
        "in a homogenuous array" in {
          val json = JsonParser("""[12, 39, 2, 5, 8, 3]""")

          "if found" in {
            json update (JsonLenses.find(JsonLenses.value.is[Int](_ < 4)) ! set("test")) must be_json(
              """[12, 39, "test", 5, 8, 3]""")
          }
          "if not found" in {
            json update (JsonLenses.find(JsonLenses.value.is[Int](_ == 434)) ! set("test")) must be_json(
              """[12, 39, 2, 5, 8, 3]""")
          }
        }
        "in an inhomogenuous array" in {
          val json = JsonParser("""["a", "b", 2, 5, 8, 3]""")

          json update (JsonLenses.find(JsonLenses.value.is[Int](_ < 4)) ! set("test")) must be_json(
            """["a", "b", "test", 5, 8, 3]"""
          )
        }
      }
    }
  }

  def be_json(json: String) =
    be_==(JsonParser(json))
}
