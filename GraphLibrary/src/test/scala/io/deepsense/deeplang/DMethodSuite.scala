/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import org.scalatest.FunSuite

class DMethodSuite extends FunSuite {
  test("It is possible to implement class having DMethod") {
    case class A(i: Int = 0) extends DOperable
    case class B(i: Int = 0) extends DOperable
    class C extends DOperable {
      val f: DMethod1To1[Int, A, B] = new DMethod1To1[Int, A, B] {
        override def apply(parameters: Int)(t0: A): B = B(t0.i + parameters)
        override def infer(parameters: Int)(k0: DKnowledge[A]): DKnowledge[B] = DKnowledge(B())
      }
    }
    val c = new C
    assert(c.f(2)(A(3)) == B(5))
    assert(c.f.infer(2)(DKnowledge(A())) == DKnowledge(B()))
  }
}
