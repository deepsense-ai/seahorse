/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import org.scalatest.FunSuite

import io.deepsense.deeplang.dhierarchy.DHierarchy

object DClassesForDMethods {
  class S extends DOperable
  case class A(i: Int) extends S { def this() = this(0) }
  case class B(i: Int) extends S { def this() = this(0) }
}

class DMethodSuite extends FunSuite {
  test("It is possible to implement class having DMethod") {
    import DClassesForDMethods._

    class C extends DOperable {
      val f: DMethod1To1[Int, A, B] = new DMethod1To1[Int, A, B] {
        override def apply(context: ExecutionContext)(parameters: Int)(t0: A): B = {
          B(t0.i + parameters)
        }
      }
    }

    val c = new C
    assert(c.f(new ExecutionContext)(2)(A(3)) == B(5))

    val h = new DHierarchy
    h.registerDOperable[A]()
    h.registerDOperable[B]()

    val context = new InferContext(h)
    assert(c.f.infer(context)(2)(DKnowledge(new A())) == DKnowledge(new B()))
  }

  test("It is possible to override inferring in DMethod") {
    import DClassesForDMethods._

    class C extends DOperable {
      val f: DMethod0To1[Int, S] = new DMethod0To1[Int, S] {
        override def apply(context: ExecutionContext)(parameters: Int)(): S = A(parameters)
        override def infer(context: InferContext)(parameters: Int)(): DKnowledge[S] = {
          DKnowledge(new A)
        }
      }
    }

    val c = new C

    val h = new DHierarchy
    h.registerDOperable[A]()
    h.registerDOperable[B]()

    val context = new InferContext(h)
    assert(c.f.infer(context)(2)() == DKnowledge(new A()))
  }
}
