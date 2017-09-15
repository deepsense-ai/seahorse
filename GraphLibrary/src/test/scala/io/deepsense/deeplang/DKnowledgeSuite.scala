/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import org.scalatest.FunSuite

/** */
class DKnowledgeSuite extends FunSuite {

  test("DKnowledge[DOperable] with same content are equal") {
    case class A(i: Int) extends DOperable
    case class B(i: Int) extends DOperable

    val knowledge1 = DKnowledge(A(1), B(2), A(3))
    val knowledge2 = DKnowledge(A(1), A(3), B(2), A(1))
    assert(knowledge1 == knowledge2)
  }

  test("DKnowledge[_] objects with same content are equal") {
    def isAOrB(any: Any) = any.isInstanceOf[A] || any.isInstanceOf[B]
    class A extends DOperable {
      override def equals(any: Any) = isAOrB(any)
    }
    class B extends DOperable {
      override def equals(any: Any) = isAOrB(any)
    }

    val knowledge1: DKnowledge[A] = DKnowledge(new A)
    val knowledge2: DKnowledge[B] = DKnowledge(new B)
    assert(knowledge1 == knowledge2)
  }

  test("DKnowledge with different content are not equal") {
    case class A(i: Int) extends DOperable

    val knowledge1 = DKnowledge(A(1))
    val knowledge2 = DKnowledge(A(2))
    assert(knowledge1 != knowledge2)
  }
}
