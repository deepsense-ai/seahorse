/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.parameters.{NumericParameter, ParametersSchema, Validator}

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
  class SimpleOperation extends DOperation1To1[A1, A2] {
    override protected def _execute(context: ExecutionContext)(t0: A1): A2 = ???
    override val name: String = ""
    override val parameters: ParametersSchema = ParametersSchema()
  }
}

class DOperationSuite extends FunSuite with MockitoSugar {

  test("It is possible to implement simple operations") {
    import DClassesForDOperations._

    case class IntParam(i: Int) extends ParametersSchema

    class PickOne extends DOperation2To1[A1, A2, A] {
      override protected def _execute(context: ExecutionContext)(t1: A1, t2: A2): A = {
        val param = parameters.getDouble("param").get
        if (param % 2 == 1) t1 else t2
      }
      override val name: String = "Some name"
      override val parameters: ParametersSchema = ParametersSchema(
        "param" -> NumericParameter(
          "description", None, required = true, validator = mock[Validator[Double]]))
    }

    val firstPicker: DOperation = new PickOne
    firstPicker.parameters.getNumericParameter("param").value = Some(1)
    val secondPicker: DOperation = new PickOne
    secondPicker.parameters.getNumericParameter("param").value = Some(2)

    val input = Vector(new A1, new A2)
    assert(firstPicker.execute(new ExecutionContext)(input) == Vector(new A1))
    assert(secondPicker.execute(new ExecutionContext)(input) == Vector(new A2))

    val h = new DOperableCatalog
    h.registerDOperable[A1]()
    h.registerDOperable[A2]()
    val context = new InferContext(h)

    val knowledge = Vector[DKnowledge[DOperable]](DKnowledge(new A1), DKnowledge(new A2))
    assert(firstPicker.inferKnowledge(context)(knowledge) == Vector(DKnowledge(new A1, new A2)))
  }

  test("It is possible to override knowledge inferring in DOperation") {
    import DClassesForDOperations._

    class GeneratorOfA extends DOperation0To1[A] {
      override protected def _execute(context: ExecutionContext)(): A = ???
      override protected def _inferKnowledge(context: InferContext)(): DKnowledge[A] = {
        new DKnowledge(new A1, new A2)
      }

      override val name: String = ""
      override val parameters: ParametersSchema = ParametersSchema()
    }

    val generator: DOperation = new GeneratorOfA

    val h = new DOperableCatalog
    h.registerDOperable[A1]()
    h.registerDOperable[A2]()
    val context = new InferContext(h)

    assert(generator.inferKnowledge(context)(Vector()) == Vector(DKnowledge(new A1, new A2)))
  }

  test("Getting types required in input port") {
    import DOperationForPortTypes._
    val op = new SimpleOperation
    assert(op.inPortTypes == Vector(ru.typeTag[DClassesForDOperations.A1]))
  }

  test("Getting types required in output port") {
    import DOperationForPortTypes._
    val op = new SimpleOperation
    assert(op.outPortTypes == Vector(ru.typeTag[DClassesForDOperations.A2]))
  }
}
