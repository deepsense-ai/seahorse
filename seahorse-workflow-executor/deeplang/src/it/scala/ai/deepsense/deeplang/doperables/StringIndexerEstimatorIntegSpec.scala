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

package ai.deepsense.deeplang.doperables

import ai.deepsense.deeplang.doperables.StringIndexerEstimatorIntegSpec._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.{NoInPlaceChoice, YesInPlaceChoice}
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.StringIndexerEstimator
import ai.deepsense.deeplang.doperables.spark.wrappers.models.{MultiColumnStringIndexerModel, SingleColumnStringIndexerModel}
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.TransformerSerialization
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.TransformerSerialization._
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.deeplang.{DKnowledge, DeeplangIntegTestSupport}

class StringIndexerEstimatorIntegSpec
  extends DeeplangIntegTestSupport
  with TransformerSerialization {

  import DeeplangIntegTestSupport._

  "StringIndexerEstimator" should {
    "convert single column" in {
      val si = new StringIndexerEstimator()
      si.setSingleColumn("a", "overriddenBelow")
      val t = si.fit(executionContext)(())(inputDataFrame)
        .asInstanceOf[SingleColumnStringIndexerModel]

      t.setInputColumn("c")
      t.setSingleInPlaceParam(NoInPlaceChoice().setOutputColumn("out"))

      val transformed = executeTransformation(t, inputDataFrame)
      assertDataFramesEqual(
        transformed,
        outputDataFrame,
        checkRowOrder = true,
        checkNullability = false)
    }
    "convert single column in-place" in {
      val si = new StringIndexerEstimator()
      si.setSingleColumn("a", "overriddenBelow")
      val t = si.fit(executionContext)(())(inputDataFrame)
        .asInstanceOf[SingleColumnStringIndexerModel]

      t.setInputColumn("c")
      t.setSingleInPlaceParam(YesInPlaceChoice())

      t.transform(executionContext)(())(inputDataFrame)

      val transformed = executeTransformation(t, inputDataFrame)
      assertDataFramesEqual(
        transformed,
        outputDataFrameInPlace,
        checkRowOrder = true,
        checkNullability = false)
    }
    "convert multiple columns" in {
      val si = new StringIndexerEstimator()
      val outputPrefix = "idx_"
      si.setMultipleColumn(Set("a", "b"), outputPrefix)

      val t = si.fit(executionContext)(())(inputDataFrame)
        .asInstanceOf[MultiColumnStringIndexerModel]

      val transformed = executeTransformation(t, inputDataFrame)
      assertDataFramesEqual(
        transformed,
        multiOutputDataFrame,
        checkRowOrder = true,
        checkNullability = false)
    }
    "convert multiple columns in-place" in {
      val si = new StringIndexerEstimator()
      si.setMultipleColumnInPlace(Set("a", "b"))

      val t = si.fit(executionContext)(())(inputDataFrame)
        .asInstanceOf[MultiColumnStringIndexerModel]

      t.validateParams shouldBe empty

      val transformed = executeTransformation(t, inputDataFrame)
      assertDataFramesEqual(
        transformed,
        multiOutputDataFrameInPlace,
        checkRowOrder = true,
        checkNullability = false)
    }
    "infer knowledge in single-column mode" in {
      val si = new StringIndexerEstimator()
      si.setSingleColumn("a", "out")

      val inputKnowledge: DKnowledge[DataFrame] = DKnowledge(Set(inputDataFrame))
      val (transformerKnowledge, _) = si.fit.infer(mock[InferContext])(())(inputKnowledge)
      val t = transformerKnowledge.single
        .asInstanceOf[SingleColumnStringIndexerModel]

      t.setInputColumn("c")
      t.setSingleInPlaceParam(NoInPlaceChoice().setOutputColumn("out"))

      val (outputKnowledge, _) = t.transform.infer(mock[InferContext])(())(inputKnowledge)
      val inferredSchema = outputKnowledge.single.schema.get
      assertSchemaEqual(inferredSchema, outputDataFrame.schema.get, checkNullability = false)
    }
    "infer knowledge in multi-column mode" in {
      val si = new StringIndexerEstimator()
      val outputPrefix = "idx_"
      si.setMultipleColumn(Set("a", "b"), outputPrefix)

      val inputKnowledge: DKnowledge[DataFrame] = DKnowledge(Set(inputDataFrame))
      val (transformerKnowledge, _) = si.fit.infer(mock[InferContext])(())(inputKnowledge)
      val inf = transformerKnowledge.single
        .asInstanceOf[MultiColumnStringIndexerModel]

      val (outputKnowledge, _) = inf.transform.infer(mock[InferContext])(())(inputKnowledge)
      val inferredSchema = outputKnowledge.single.schema.get
      assertSchemaEqual(inferredSchema, multiOutputDataFrame.schema.get, checkNullability = false)
    }
  }

  private def executeTransformation(
      transformer: Transformer,
      inputDataFrame: DataFrame): DataFrame = {
    transformer.applyTransformationAndSerialization(tempDir, inputDataFrame)
  }

  val testRows = Seq(
    TestRow("a",   "bb",  "a"),
    TestRow("aa",  "b",   "a"),
    TestRow("a",   "b",   "a"),
    TestRow("aaa", "b",   "aa"),
    TestRow("aa",  "bbb", "aaa"),
    TestRow("aa",  "b",   "aaa")
  )

  val inputDataFrame = createDataFrame(testRows)

  val indexesA: Map[String, Double] = Map(
    ("a", 1),
    ("aa", 0),
    ("aaa", 2))

  val outputDataFrame = {
    val indexedRs = testRows.map {
      case TestRow(a, b, c) =>
        IndexedRow(a, b, c, indexesA(c))
    }
    createDataFrame(indexedRs)
  }

  val outputDataFrameInPlace = {
    val indexedRs = testRows.map {
      case TestRow(a, b, c) =>
        IndexedRowInPlace(a, b, indexesA(c))
    }
    createDataFrame(indexedRs)
  }

  val multiOutputDataFrame = {
    val indexesB: Map[String, Double] = Map(
      ("b", 0),
      ("bb", 1),
      ("bbb", 2)
    )

    val indexedRs = testRows.map {
      case TestRow(a, b, c) =>
        MultiIndexedRow(a, b, c, indexesA(a), indexesB(b))
    }

    createDataFrame(indexedRs)
  }

  val multiOutputDataFrameInPlace = {
    val indexesB: Map[String, Double] = Map(
      ("b", 0),
      ("bb", 1),
      ("bbb", 2)
    )

    val indexedRs = testRows.map {
      case TestRow(a, b, c) =>
        MultiIndexedInPlaceRow(indexesA(a), indexesB(b), c)
    }

    createDataFrame(indexedRs)
  }
}

object StringIndexerEstimatorIntegSpec {
  case class TestRow(a: String, b: String, c: String)
  case class IndexedRow(a: String, b: String, c: String, out: Double)
  case class IndexedRowInPlace(a: String, b: String, c: Double)
  case class MultiIndexedRow(a: String, b: String, c: String, idx_a: Double, idx_b: Double)
  case class MultiIndexedInPlaceRow(a: Double, b: Double, c: String)
}
