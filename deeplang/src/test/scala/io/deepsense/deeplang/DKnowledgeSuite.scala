/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

import org.scalatest.FunSuite

import io.deepsense.deeplang.doperables.Report

object ClassesForDKnowledge {
  trait A extends DOperable {
    override def report: Report = ???
  }
  trait B extends DOperable {
    override def report: Report = ???
  }
  case class A1(i: Int) extends A
  case class A2(i: Int) extends A
  case class B1(i: Int) extends B
  case class B2(i: Int) extends B
}

class DKnowledgeSuite extends FunSuite {

  test("DKnowledge[DOperable] with same content are equal") {
    case class A(i: Int) extends DOperable {
      override def report: Report = ???
    }
    case class B(i: Int) extends DOperable {
      override def report: Report = ???
    }

    val knowledge1 = DKnowledge(A(1), B(2), A(3))
    val knowledge2 = DKnowledge(A(1), A(3), B(2), A(1))
    assert(knowledge1 == knowledge2)
  }

  test("DKnowledge[_] objects with same content are equal") {
    def isAOrB(any: Any) = any.isInstanceOf[A] || any.isInstanceOf[B]
    class A extends DOperable {
      override def report: Report = ???
      override def equals(any: Any) = isAOrB(any)
    }
    class B extends DOperable {
      override def report: Report = ???
      override def equals(any: Any) = isAOrB(any)
    }

    val knowledge1: DKnowledge[A] = DKnowledge(new A)
    val knowledge2: DKnowledge[B] = DKnowledge(new B)
    assert(knowledge1 == knowledge2)
  }

  test("DKnowledge with different content are not equal") {
    case class A(i: Int) extends DOperable {
      override def report: Report = ???
    }

    val knowledge1 = DKnowledge(A(1))
    val knowledge2 = DKnowledge(A(2))
    assert(knowledge1 != knowledge2)
  }

  test("DKnowledge can intersect internal knowledge with external types") {
    import ClassesForDKnowledge._
    val knowledge = DKnowledge(A1(1), new A2(2), new B1(1), new B2(2))
    assert(knowledge.filterTypes(ru.typeOf[A]) == DKnowledge(new A1(1), new A2(2)))
  }
}
