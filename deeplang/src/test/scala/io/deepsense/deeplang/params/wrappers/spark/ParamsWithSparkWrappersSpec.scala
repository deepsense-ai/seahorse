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

package io.deepsense.deeplang.params.wrappers.spark

import org.apache.spark.ml
import org.apache.spark.ml.param._
import org.apache.spark.sql.types.StructType
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import io.deepsense.deeplang.params.BooleanParam

class ParamsWithSparkWrappersSpec extends WordSpec
  with Matchers
  with MockitoSugar {

  class ExampleSparkParams extends ml.param.Params {
    override val uid: String = "id"
    val sparkParamA = new Param[String]("", "paramA", "descA")
    val sparkParamB = new IntParam("", "paramB", "descB")

    override def copy(extra: ParamMap): Params = ???
  }

  case class ParamsWithSparkWrappersClass() extends ParamsWithSparkWrappers {

    val exampleSparkParams = new ExampleSparkParams

    val paramA = new StringParamWrapper[ExampleSparkParams]("paramA", "descA", _.sparkParamA)
    val paramB = new IntParamWrapper[ExampleSparkParams]("paramB", "descB", _.sparkParamB)
    val notWrappedParam = BooleanParam("booleanParamName", "booleanParamDescription")

    val params: Array[io.deepsense.deeplang.params.Param[_]] =
      Array(paramA, paramB, notWrappedParam)

    def setParamA(v: String): this.type = set(paramA, v)
    def setParamB(v: Double): this.type = set(paramB, v)
  }

  "ParamsWithSparkWrappers" should {
    "calculate sparkParamWrappers" in {
      val paramsWithSparkWrappers = ParamsWithSparkWrappersClass()
      paramsWithSparkWrappers.sparkParamWrappers shouldBe
        Array(paramsWithSparkWrappers.paramA, paramsWithSparkWrappers.paramB)
    }
    "return parameter values" in {
      val paramsWithSparkWrappers = ParamsWithSparkWrappersClass().setParamA("a").setParamB(0.0)
      paramsWithSparkWrappers.sparkParamMap(
        paramsWithSparkWrappers.exampleSparkParams, StructType(Seq())).toSeq.toSet shouldBe
        Set(
          paramsWithSparkWrappers.exampleSparkParams.sparkParamA -> "a",
          paramsWithSparkWrappers.exampleSparkParams.sparkParamB -> 0)
    }
  }
}
