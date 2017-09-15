/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

import org.scalatest.FunSuite

import io.deepsense.deeplang.dhierarchy.DHierarchy

object DClassesForDOperations {
  trait A extends DOperable
  class A1 extends A {
    override def equals(any: Any) = any.isInstanceOf[A1]
  }
  class A2 extends A {
    override def equals(any: Any) = any.isInstanceOf[A2]
  }
}

object DOperationForPortTypes {
  import DClassesForDOperations._
  class SimpleOperation extends DOperation1To1[A1, A2](null) {
    override protected def _execute(t0: A1): A2 = ???
  }
}

class DOperationSuite extends FunSuite {

  test("It is possible to implement simple operations") {
    import DClassesForDOperations._

    case class IntParam(i: Int) extends DParameters

    class PickOne(p: DParameters) extends DOperation2To1[A1, A2, A](p) {
      override protected def _execute(t1: A1, t2: A2): A = {
        val intParam = p.asInstanceOf[IntParam]
        if (intParam.i % 2 == 1) t1 else t2
      }
    }

    val firstPicker: DOperation = new PickOne(IntParam(1))
    val secondPicker: DOperation = new PickOne(IntParam(2))

    val input = Vector(new A1, new A2)
    assert(firstPicker.execute(input) == Vector(new A1))
    assert(secondPicker.execute(input) == Vector(new A2))

    val h = new DHierarchy
    h.registerDOperable[A1]()
    h.registerDOperable[A2]()
    val context = new InferContext(h)

    val knowledge = Vector[DKnowledge[DOperable]](DKnowledge(new A1), DKnowledge(new A2))
    assert(firstPicker.inferKnowledge(context)(knowledge) == Vector(DKnowledge(new A1, new A2)))
  }

  test("It is possible to override knowledge inferring in DOperation") {
    import DClassesForDOperations._

    class GeneratorOfA extends DOperation0To1[A](null) {
      override protected def _execute(): A = ???
      override protected def _inferKnowledge(context: InferContext)(): DKnowledge[A] = {
        new DKnowledge(new A1, new A2)
      }
    }

    val generator: DOperation = new GeneratorOfA

    val h = new DHierarchy
    h.registerDOperable[A1]()
    h.registerDOperable[A2]()
    val context = new InferContext(h)

    assert(generator.inferKnowledge(context)(Vector()) == Vector(DKnowledge(new A1, new A2)))
  }

  test("Getting type required in input port") {
    import DOperationForPortTypes._
    val op = new SimpleOperation
    assert(op.inPortType(0) == ru.typeTag[DClassesForDOperations.A1])
  }

  test("Getting type required in output port") {
    import DOperationForPortTypes._
    val op = new SimpleOperation
    assert(op.outPortType(0) == ru.typeTag[DClassesForDOperations.A2])
  }

  test("Requesting type of non-existing input port throws") {
    intercept[IllegalArgumentException] {
      import DOperationForPortTypes._
      val op = new SimpleOperation
      op.inPortType(-1)
    }
  }

  test("Requesting type of non-existing output port throws") {
    intercept[IllegalArgumentException] {
      import DOperationForPortTypes._
      val op = new SimpleOperation
      op.outPortType(2)
    }
  }
}
