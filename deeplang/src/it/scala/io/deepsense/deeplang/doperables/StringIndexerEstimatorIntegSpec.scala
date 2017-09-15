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

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.doperables.StringIndexerEstimatorIntegSpec.{IndexedR, MultiIndexedR, R}
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.{DKnowledge, DeeplangIntegTestSupport}

class StringIndexerEstimatorIntegSpec extends DeeplangIntegTestSupport {

  "StringIndexerEstimator" should {
    "convert single column" in {
      val si = new StringIndexerEstimator()
      si.setSingleColumn("a", "overriddenBelow")
      val t = si.fit(executionContext)(())(inputDataFrame)
        .asInstanceOf[SingleStringIndexerModel]

      t.setInputColumn("c")
      t.setSingleInPlaceParam(NoInPlaceChoice().setOutputColumn("out"))

      val transformed = t.transform(executionContext)(())(inputDataFrame)
      assertDataFramesEqual(
        transformed,
        outputDataFrame,
        checkRowOrder = true,
        checkNullability = false)
    }
    "convert multiple columns" in {
      val si = new StringIndexerEstimator()
      val outputPrefix = "idx"
      si.setMultipleColumn(Set("a", "b"), outputPrefix)

      val t = si.fit(executionContext)(())(inputDataFrame)
        .asInstanceOf[StringIndexerModel]

      val transformed = t.transform(executionContext)(())(inputDataFrame)
      assertDataFramesEqual(
        transformed,
        multiOutputDataFrame,
        checkRowOrder = true,
        checkNullability = false)
    }
    "infer knowledge in single-column mode" in {
      val si = new StringIndexerEstimator()
      si.setSingleColumn("a", "out")

      val inputKnowledge: DKnowledge[DataFrame] = DKnowledge(Set(inputDataFrame))
      val (transformerKnowledge, _) = si.fit.infer(mock[InferContext])(())(inputKnowledge)
      val t = transformerKnowledge.single
        .asInstanceOf[SingleStringIndexerModel]

      t.setInputColumn("c")
      t.setSingleInPlaceParam(NoInPlaceChoice().setOutputColumn("out"))

      val (outputKnowledge, _) = t.transform.infer(mock[InferContext])(())(inputKnowledge)
      val inferredSchema = outputKnowledge.single.schema.get
      assertSchemaEqual(inferredSchema, outputDataFrame.schema.get)
    }
    "infer knowledge in multi-column mode" in {
      val si = new StringIndexerEstimator()
      val outputPrefix = "idx"
      si.setMultipleColumn(Set("a", "b"), outputPrefix)

      val inputKnowledge: DKnowledge[DataFrame] = DKnowledge(Set(inputDataFrame))
      val (transformerKnowledge, _) = si.fit.infer(mock[InferContext])(())(inputKnowledge)
      val inf = transformerKnowledge.single
        .asInstanceOf[StringIndexerModel]

      val (outputKnowledge, _) = inf.transform.infer(mock[InferContext])(())(inputKnowledge)
      val inferredSchema = outputKnowledge.single.schema.get
      assertSchemaEqual(inferredSchema, multiOutputDataFrame.schema.get)
    }
  }

  val rs = Seq(
    R("a",   "bb",  "a"),
    R("aa",  "b",   "a"),
    R("a",   "b",   "a"),
    R("aaa", "b",   "aa"),
    R("aa",  "bbb", "aaa"),
    R("aa",  "b",   "aaa")
  )

  val inputDataFrame = createDataFrame(rs)

  val indexesA: Map[String, Double] = Map(
    ("a", 1),
    ("aa", 0),
    ("aaa", 2))

  val outputDataFrame = {
    val indexedRs = rs.map {
      case R(a, b, c) =>
        IndexedR(a, b, c, indexesA(c))
    }
    createDataFrame(indexedRs)
  }

  val multiOutputDataFrame = {
    val indexesB: Map[String, Double] = Map(
      ("b", 0),
      ("bb", 1),
      ("bbb", 2)
    )

    val indexedRs = rs.map {
      case R(a, b, c) =>
        MultiIndexedR(a, b, c, indexesA(a), indexesB(b))
    }

    createDataFrame(indexedRs)
  }
}

object StringIndexerEstimatorIntegSpec {
  case class R(a: String, b: String, c: String)
  case class IndexedR(a: String, b: String, c: String, out: Double)
  case class MultiIndexedR(a: String, b: String, c: String, idx_a: Double, idx_b: Double)
}
