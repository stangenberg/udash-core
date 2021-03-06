package io.udash.properties

import io.udash.testing.UdashFrontendTest

class PropertyCreatorTest extends UdashFrontendTest {
  "PropertyCreator" should {
    "create Property for basic types (and handle init value)" in {
      """val p = Property[String]""".stripMargin should compile

      """val p = Property[String]("ABC")""".stripMargin should compile

      """val p = Property[String](2)""".stripMargin shouldNot typeCheck

      """val p = Property[Int](2)""".stripMargin should compile
    }

    "create Property for case class" in {
      """case class A(s: String, i: Int)
        |val p = Property[A]""".stripMargin should compile

      """case class A(s: String, i: Int)
        |val p = Property[A](A("bla", 5))""".stripMargin should compile

      """case class A(s: String, i: Int)
        |val p = Property[A](5)""".stripMargin shouldNot typeCheck

      """case class A(s: String, i: Int)
        |val p = Property[A]("bla")""".stripMargin shouldNot typeCheck
    }

    "create Property for sealed trait" in {
      """sealed trait T
        |case object A extends T
        |case object B extends T
        |val p = Property[T]""".stripMargin should compile

      """sealed trait T
        |case object A extends T
        |case object B extends T
        |val p = Property[T](A)""".stripMargin should compile

      """sealed trait T
        |case object A extends T
        |case object B extends T
        |val p = Property[T](B)""".stripMargin should compile

      """sealed trait T
        |case object A extends T
        |case object B extends T
        |case class C(t: T) extends T
        |val p = Property[T](C(C(A)))""".stripMargin should compile

      """sealed trait T
        |case object A extends T
        |case object B extends T
        |case object C
        |val p = Property[T](C)""".stripMargin shouldNot typeCheck
    }

    "create ModelProperty for trait" in {
      """trait T {
        |  def i: Int
        |  def s: String
        |}
        |val p = Property[T].asModel""".stripMargin should compile

      """trait T {
        |  def i: Int
        |  def s: String
        |  def x: Int = 5
        |}
        |val p = Property[T].asModel""".stripMargin should compile

      """trait X {
        |  def a: String
        |}
        |
        |trait T {
        |  def i: Int
        |  def s: String
        |  def x: X
        |}
        |val p = Property[T].asModel
        |val x = p.subModel(_.x)""".stripMargin should compile
    }

    "create ModelProperty for recursive trait" in {
      """trait T {
        |  def i: Int
        |  def s: String
        |  def t: T
        |}
        |val p = Property[T].asModel""".stripMargin should compile

      """trait X {
        |  def a: String
        |  def t: T
        |}
        |
        |trait T {
        |  def i: Int
        |  def s: String
        |  def x: X
        |}
        |val p = Property[T].asModel
        |val x = p.subModel(_.x)""".stripMargin should compile
    }

    "not create ModelProperty for anything other than trait" in {
      """val p = Property[Int].asModel""".stripMargin shouldNot compile

      """val p = Property[String].asModel""".stripMargin shouldNot compile

      """val p = Property[Seq[Int]].asModel""".stripMargin shouldNot compile

      """sealed trait T
        |case object A extends T
        |case object B extends T
        |val p = Property[T].asModel""".stripMargin shouldNot compile

      """sealed trait T
        |case object A extends T
        |case object B extends T
        |val p = Property[Seq[T]].asModel""".stripMargin shouldNot compile

      """case class A(s: String, i: Int)
        |val p = Property[A].asModel""".stripMargin shouldNot compile

      """case class A(s: String, i: Int)
        |val p = Property[Seq[A]].asModel""".stripMargin shouldNot compile

      """trait T {
        |  def i: Int
        |  def s: String
        |}
        |val p = Property[Seq[T]].asModel""".stripMargin shouldNot compile
    }

    "create SeqProperty for Seq" in {
      """val p = Property[Seq[Int]].asSeq[Int]""".stripMargin should compile

      """val p = Property[Seq[Seq[Int]]](Seq(Seq(1,2))).asSeq[Seq[Int]]
        |val p2 = p.elemProperties.head.asSeq[Int]
        |val i: Property[Int] = p2.elemProperties.head
        |i.set(5)""".stripMargin should compile

      """trait T {
        |  def i: Int
        |  def s: String
        |}
        |val p = Property[Seq[T]].asSeq[T]""".stripMargin should compile

      """trait T {
        |  def i: Int
        |  def s: String
        |}
        |val p = Property[Seq[Seq[T]]].asSeq[T]""".stripMargin shouldNot compile

      """trait T {
        |  def i: Int
        |  def s: String
        |  def t: T
        |}
        |val p = Property[Seq[T]].asSeq[T]
        |val m: ModelProperty[T] = p.elemProperties.head.asModel
        |val sub = m.subProp(_.s)
        |val sub2 = m.subModel(_.t)""".stripMargin should compile

      """trait T {
        |  def i: Int
        |  def s: String
        |  def t: T
        |}
        |
        |val p = Property[Seq[Seq[T]]].asSeq[Seq[T]]
        |val p2 = p.elemProperties.head.asSeq[T]
        |val m: ModelProperty[T] = p2.elemProperties.head.asModel
        |val sub = m.subProp(_.s)
        |val sub2 = m.subModel(_.t)""".stripMargin should compile

      """trait X {
        |  def a: String
        |}
        |
        |trait T {
        |  def i: Int
        |  def s: String
        |  def x: X
        |}
        |val p = Property[Seq[T]].asSeq[T]""".stripMargin should compile

      """trait X {
        |  def a: String
        |  def t: T
        |  def x: X
        |  def st: Seq[T]
        |}
        |
        |trait T {
        |  def i: Int
        |  def s: String
        |  def t: T
        |  def x: X
        |  def sx: Seq[X]
        |}
        |val p = Property[Seq[T]].asSeq[T]
        |val p2 = Property[Seq[Seq[T]]].asSeq[Seq[T]]""".stripMargin should compile

      """sealed trait T
        |case object A extends T
        |case object B extends T
        |val p = Property[Seq[T]].asSeq[T]
        |val m: Property[T] = p.elemProperties.head""".stripMargin should compile

      """sealed trait T
        |case object A extends T
        |case object B extends T
        |val p = Property[Seq[T]].asSeq[T]
        |val m: ModelProperty[T] = p.elemProperties.head.asModel""".stripMargin shouldNot compile
    }

    "not create SeqProperty for anything other than Seq" in {
      """val p = Property[Int].asSeq""".stripMargin shouldNot compile

      """val p = Property[String].asSeq""".stripMargin shouldNot compile

      """sealed trait T
        |case object A extends T
        |case object B extends T
        |val p = Property[T].asSeq""".stripMargin shouldNot compile

      """sealed trait T
        |case object A extends T
        |case object B extends T
        |val p = Property[Seq[T]].asSeq""".stripMargin shouldNot compile

      """case class A(s: String, i: Int)
        |val p = Property[A].asSeq""".stripMargin shouldNot compile

      """case class A(s: String, i: Int)
        |val p = Property[Seq[A]].asSeq""".stripMargin shouldNot compile

      """trait T {
        |  def i: Int
        |  def s: String
        |}
        |val p = Property[Seq[T]].asSeq""".stripMargin shouldNot compile

      """trait T {
        |  def i: Seq[Int]
        |  def s: String
        |  def t: T
        |}
        |val p = Property[T].asSeq""".stripMargin shouldNot compile

      """trait X {
        |  def a: String
        |  def t: T
        |}
        |
        |trait T {
        |  def i: Int
        |  def s: String
        |  def x: Seq[X]
        |}
        |val p = Property[T].asSeq""".stripMargin shouldNot compile
    }

    "create complex properties" in {
      """case class C(i: Int, s: String)
        |trait T {
        |  def i: Int
        |  def s: Option[String]
        |  def t: ST
        |}
        |trait ST {
        |  def c: C
        |  def s: Seq[Char]
        |}
        |val p = Property[T].asModel
        |val s = p.subModel(_.t).subSeq(_.s)
        |val s2 = p.subSeq(_.t.s)""".stripMargin should compile
    }

    "not create property for mutable class" in {
      """val p = Property[scala.collection.mutable.ArrayBuffer[Int]]""".stripMargin shouldNot compile

      """class C {
        |  var i = 0
        |  def inc() = i += 1
        |}
        |val p = Property[C]""".stripMargin shouldNot compile

      """trait T {
        |  var i = 0
        |}
        |class C extends T {
        |  def inc() = i += 1
        |}
        |val p = Property[C]""".stripMargin shouldNot compile

      """import org.scalajs.dom.Element
        |trait T {
        |  def elements: Seq[Element]
        |}
        |val p = ModelProperty[T]""".stripMargin should compile
    }
  }
}
