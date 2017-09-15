/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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


import scala.reflect.runtime.universe.typeTag

class TypeUtilsSpec extends UnitSpec {

  import TypeUtilsSpec._

  "TypeUtils.describeType" should {
    "describe class" in {
      TypeUtils.describeType(typeTag[A].tpe) shouldBe Seq(describedA)
    }
    "describe trait" in {
      TypeUtils.describeType(typeTag[B].tpe) shouldBe Seq(describedB)
    }
    "describe complex type" in {
      TypeUtils.describeType(typeTag[A with B].tpe) shouldBe Seq(describedA, describedB)
    }
    "describe parametrized type" in {
      TypeUtils.describeType(typeTag[C[A]].tpe) shouldBe Seq(describedC)
    }
    "describe complex parametrized type" in {
      TypeUtils.describeType(typeTag[C[A] with B].tpe) shouldBe Seq(describedC, describedB)
    }
  }
}

object TypeUtilsSpec {
  class A
  trait B
  class C[T]

  val describedA = "ai.deepsense.deeplang.TypeUtilsSpec.A"
  val describedB = "ai.deepsense.deeplang.TypeUtilsSpec.B"
  val describedC = "ai.deepsense.deeplang.TypeUtilsSpec.C"

}
