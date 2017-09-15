/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang

import org.scalatest.FunSuite

import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.doperables.DOperableMock
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}

object DClassesForDMethods {
  class S extends DOperableMock
  case class A(i: Int) extends S { def this() = this(0) }
  case class B(i: Int) extends S { def this() = this(0) }
}

class DMethodSuite extends FunSuite with DeeplangTestSupport {

  test("It is possible to implement class having DMethod") {
    import DClassesForDMethods._

    class C extends DOperableMock {
      val f: DMethod1To1[Int, A, B] = new DMethod1To1[Int, A, B] {
        override def apply(context: ExecutionContext)(parameters: Int)(t0: A): B = {
          B(t0.i + parameters)
        }
      }
    }

    val c = new C
    assert(c.f(mock[ExecutionContext])(2)(A(3)) == B(5))

    val h = new DOperableCatalog
    h.registerDOperable[A]()
    h.registerDOperable[B]()

    val context = createInferContext(h)
    val (result, warnings) = c.f.infer(context)(2)(DKnowledge(new A()))
    assert(result == DKnowledge(new B()))
    assert(warnings == InferenceWarnings.empty)
  }

  test("It is possible to override inferring in DMethod") {
    import DClassesForDMethods._

    val mockedWarnings = mock[InferenceWarnings]

    class C extends DOperableMock {
      val f: DMethod0To1[Int, S] = new DMethod0To1[Int, S] {
        override def apply(context: ExecutionContext)(parameters: Int)(): S = A(parameters)
        override def infer(context: InferContext)(parameters: Int)()
            : (DKnowledge[S], InferenceWarnings) = {
          (DKnowledge(new A), mockedWarnings)
        }
      }
    }

    val c = new C

    val h = new DOperableCatalog
    h.registerDOperable[A]()
    h.registerDOperable[B]()

    val context = createInferContext(h)
    val (result, warnings) = c.f.infer(context)(2)()
    assert(result == DKnowledge(new A()))
    assert(warnings == mockedWarnings)
  }
}
