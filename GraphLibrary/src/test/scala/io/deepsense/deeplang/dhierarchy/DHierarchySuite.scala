/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

import org.scalatest.{FunSuite, Matchers}

import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.dhierarchy.exceptions._

object H {
  trait T1 extends DOperable
  trait T2 extends T1
  trait T3 extends T1
  trait T extends DOperable
  abstract class A extends T3
  class B extends A with T {
    override def equals(any: Any) = any.isInstanceOf[B]
  }
  class C extends A with T2 {
    override def equals(any: Any) = any.isInstanceOf[C]
  }
}

object Parametrized {
  trait T[T] extends DOperable
  abstract class A[T] extends DOperable
  class B extends A[Int]
}

object Constructors {
  class NotParameterLess(val i: Int) extends DOperable
  class AuxiliaryParameterLess(val i: Int) extends DOperable {
    def this() = this(1)
  }
  class WithDefault(val i: Int = 1) extends DOperable
}

class DHierarchySuite extends FunSuite with Matchers {

  def testGettingSubclasses[T <: DOperable : ru.TypeTag](
      h: DHierarchy, expected: DOperable*): Unit = {
    h.concreteSubclassesInstances[T] should contain theSameElementsAs expected
  }

  test("Getting concrete subclasses instances") {

    val h = new DHierarchy
    h.registerDOperable[H.B]()
    h.registerDOperable[H.C]()

    val b = new H.B
    val c = new H.C

    def check[T <: DOperable : ru.TypeTag](expected: DOperable*) = {
      testGettingSubclasses[T](h, expected:_*)
    }

    check[H.T with H.T1](b)
    check[H.T2 with H.T3](c)
    check[H.B](b)
    check[H.C](c)
    check[H.A with H.T2](c)
    check[H.A with H.T1](b, c)
    check[H.A](b, c)
    check[H.T3](b, c)
    check[H.T with H.T2]()
  }

  test("Listing DTraits and DClasses") {
    val h = new DHierarchy
    h.registerDOperable[H.B]()
    h.registerDOperable[H.C]()

    val traitsMock = TraitInfo("DOperable", Nil)::
      TraitInfo("T2", List("T1"))::
      TraitInfo("T", List("DOperable"))::
      TraitInfo("T1", List("DOperable"))::
      TraitInfo("T3", List("T1"))::
      Nil

    val classesMock = ClassInfo("A", None, List("T3"))::
      ClassInfo("B", Some("A"), List("T"))::
      ClassInfo("C", Some("A"), List("T2"))::
      Nil

    val (traitsInfo, classesInfo) = h.info
    traitsInfo should contain theSameElementsAs traitsMock
    classesInfo should contain theSameElementsAs classesMock

  }

  test("Registering class extending parametrized class should produce exception") {
    intercept[ParametrizedTypeException] {
      import Parametrized._
      val p = new DHierarchy
      p.registerDOperable[B]()
    }
  }

  test("Registering parametrized class should produce exception") {
    intercept[ParametrizedTypeException] {
      import Parametrized._
      val p = new DHierarchy
      p.registerDOperable[A[Int]]()
    }
  }

  test("Registering parametrized trait should produce exception") {
    intercept[ParametrizedTypeException] {
      import Parametrized._
      val p = new DHierarchy
      p.registerDOperable[T[Int]]()
    }
  }

  test("Registering concrete class with no parameter-less constructor should produce exception") {
    intercept[NoParameterLessConstructorException] {
      import Constructors._
      val h = new DHierarchy
      h.registerDOperable[NotParameterLess]()
    }
  }

  test("Registering class with constructor with default parameters should produce exception") {
    intercept[NoParameterLessConstructorException] {
      import Constructors._
      val h = new DHierarchy
      h.registerDOperable[WithDefault]()
    }
  }

  test("Registering class with auxiliary parameter-less constructor should succeed") {
    import Constructors._
    val h = new DHierarchy
    h.registerDOperable[AuxiliaryParameterLess]()
  }
}
