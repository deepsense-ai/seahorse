/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

import org.scalatest.{FunSuite, Matchers}

import io.deepsense.deeplang.DOperable

object H {
  trait T1 extends DOperable
  trait T2 extends T1
  trait T3 extends T1
  trait T extends DOperable
  class A extends T3
  class B extends A with T {
    override def equals(any: Any) = any.isInstanceOf[B]
  }
  class C extends A with T2 {
    override def equals(any: Any) = any.isInstanceOf[C]
  }
}

object Parametrized {
  class A[T] extends DOperable
  class B extends A[Int]
}

class DHierarchySuite extends FunSuite with Matchers {

  def testGettingSubclasses[SomeT : ru.TypeTag](h: DHierarchy, expected: DOperable*): Unit = {
    val result: mutable.Set[DOperable] = h.concreteSubclassesInstances[SomeT]
    assert(result == expected.toSet)
  }

  test("Simple hierarchy") {

    val h = new DHierarchy
    h.registerDOperable[H.B]()
    h.registerDOperable[H.C]()

    val b = new H.B
    val c = new H.C

    def check[SomeT : ru.TypeTag](expected: DOperable*) = {
      testGettingSubclasses[SomeT](h, expected:_*)
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
    intercept[RuntimeException] {
      import Parametrized._
      val p = new DHierarchy
      p.registerDOperable[B]()
    }
  }

  test("Registering parametrized class should produce exception") {
    intercept[RuntimeException] {
      import Parametrized._
      val p = new DHierarchy
      p.registerDOperable[A[Int]]()
    }
  }
}
