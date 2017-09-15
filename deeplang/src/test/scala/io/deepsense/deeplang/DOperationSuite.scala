/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}
import org.scalatest.FunSuite

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.DOperableMock
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.NumericParam
import io.deepsense.deeplang.params.validators.RangeValidator

object DClassesForDOperations {
  trait A extends DOperableMock
  case class A1() extends A
  case class A2() extends A
}

object DOperationForPortTypes {
  import DClassesForDOperations._
  class SimpleOperation extends DOperation1To1[A1, A2] {
    override protected def execute(t0: A1)(context: ExecutionContext): A2 = ???
    override val id: DOperation.Id = DOperation.Id.randomId
    override val name: String = ""
    override val description: String = ""
    override val params: Array[io.deepsense.deeplang.params.Param[_]] = Array()
  }
}

class DOperationSuite extends FunSuite with DeeplangTestSupport {

  test("It is possible to implement simple operations") {
    import DClassesForDOperations._

    class PickOne extends DOperation2To1[A1, A2, A] {
      override val id: DOperation.Id = DOperation.Id.randomId

      val param = NumericParam("param", "description", RangeValidator.all)
      def setParam(int: Int): this.type = set(param -> int)

      val params: Array[io.deepsense.deeplang.params.Param[_]] = Array(param)

      override protected def execute(t1: A1, t2: A2)(context: ExecutionContext): A = {
        if ($(param) % 2 == 1) t1 else t2
      }
      override val name: String = "Some name"
      override val description: String = "Some description"
    }

    val firstPicker = new PickOne
    firstPicker.setParam(1)
    val secondPicker = new PickOne
    secondPicker.setParam(2)

    val input = Vector(A1(), A2())
    assert(firstPicker.executeUntyped(input)(mock[ExecutionContext]) == Vector(A1()))
    assert(secondPicker.executeUntyped(input)(mock[ExecutionContext]) == Vector(A2()))

    val h = new DOperableCatalog
    h.registerDOperable[A1]()
    h.registerDOperable[A2]()
    val context = createInferContext(h)

    val knowledge = Vector[DKnowledge[DOperable]](DKnowledge(A1()), DKnowledge(A2()))
    val (result, warnings) = firstPicker.inferKnowledgeUntyped(knowledge)(context)
    assert(result == Vector(DKnowledge(A1(), A2())))
    assert(warnings == InferenceWarnings.empty)
  }

  test("It is possible to override knowledge inferring in DOperation") {
    import DClassesForDOperations._

    val mockedWarnings = mock[InferenceWarnings]

    class GeneratorOfA extends DOperation0To1[A] {
      override val id: DOperation.Id = DOperation.Id.randomId

      override protected def execute()(context: ExecutionContext): A = ???
      override protected def inferKnowledge()(context: InferContext)
          : (DKnowledge[A], InferenceWarnings) = {
        (DKnowledge(A1(), A2()), mockedWarnings)
      }

      override val name: String = ""
      override val description: String = ""

      val params: Array[io.deepsense.deeplang.params.Param[_]] = Array()
    }

    val generator: DOperation = new GeneratorOfA

    val h = new DOperableCatalog
    h.registerDOperable[A1]()
    h.registerDOperable[A2]()
    val context = createInferContext(h)

    val (results, warnings) = generator.inferKnowledgeUntyped(Vector())(context)
    assert(results == Vector(DKnowledge(A1(), A2())))
    assert(warnings == mockedWarnings)
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
