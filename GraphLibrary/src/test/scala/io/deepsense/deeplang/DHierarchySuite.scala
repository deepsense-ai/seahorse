/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import org.scalatest.FunSuite
import scala.collection.mutable
import scala.reflect.runtime.{universe=>ru}

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

class DHierarchySuite extends FunSuite {

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
}
