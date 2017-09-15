/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import org.scalatest.FunSuite

/** */
class DOperationSuite extends FunSuite {

  test("Simple operation") {
    trait A extends DOperable
    class A1 extends A
    class A2 extends A
    class B extends DOperable
    class B1 extends B
    class B2 extends B

    class Op1(p: DParameters = null) extends DOperation1To1[A, B](p) {
      override protected def _execute(t0: A): B = new B1

      override protected def _inferTypes(k: DKnowledge[A]): DKnowledge[B] = {
        new DKnowledge(new B1, new B2)
      }
    }

    val op = new Op1
    op.execute(Vector(new A1))
    op.inferTypes(Vector(new DKnowledge(new A1)))
  }
}
