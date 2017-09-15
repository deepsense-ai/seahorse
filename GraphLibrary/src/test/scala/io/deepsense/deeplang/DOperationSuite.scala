/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import org.scalatest.FunSuite

class DOperationSuite extends FunSuite {

  test("It is possible to implement simple operations") {
    trait A extends DOperable
    class A1 extends A {
      override def equals(any: Any) = any.isInstanceOf[A1]
    }
    class A2 extends A {
      override def equals(any: Any) = any.isInstanceOf[A2]
    }
    case class IntParam(i: Int) extends DParameters

    class PickOne(p: DParameters) extends DOperation2To1[A1, A2, A](p) {
      override protected def _execute(t1: A1, t2: A2): A = {
        val intParam = p.asInstanceOf[IntParam]
        if (intParam.i % 2 == 1) t1 else t2
      }

      override protected def _inferKnowledge(
          k1: DKnowledge[A1], k2: DKnowledge[A2]): DKnowledge[A] = {
        new DKnowledge(new A1, new A2)
      }
    }

    val firstPicker: DOperation = new PickOne(IntParam(1))
    val secondPicker: DOperation = new PickOne(IntParam(2))

    val input = Vector(new A1, new A2)
    assert(firstPicker.execute(input) == Vector(new A1))
    assert(secondPicker.execute(input) == Vector(new A2))

    val knowledge = Vector[DKnowledge[DOperable]](DKnowledge(new A1), DKnowledge(new A2))
    assert(firstPicker.inferKnowledge(knowledge) == Vector(DKnowledge(new A1, new A2)))
  }
}

