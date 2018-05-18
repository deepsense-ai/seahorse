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

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators

import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.DeeplangIntegTestSupport
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators.AbstractEstimatorModelWrapperSmokeTest.TestDataFrameRow
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.TransformerSerialization
import ai.deepsense.deeplang.doperables.{Estimator, Transformer}
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.sparkutils.Linalg
import ai.deepsense.sparkutils.Linalg.Vectors

abstract class AbstractEstimatorModelWrapperSmokeTest
  extends DeeplangIntegTestSupport
  with TransformerSerialization {

  import DeeplangIntegTestSupport._
  import TransformerSerialization._

  def className: String

  val estimator: Estimator[Transformer]

  val estimatorParams: Seq[ParamPair[_]]

  val dataFrame: DataFrame = {
    val rowSeq = Seq(
      TestDataFrameRow(0.0, 0.5, Vectors.dense(1.0, 2.0, 3.0), 0, 0, 0.2, 1.0, 0.5,
        Seq("a", "a", "a", "b", "b", "c").toArray, Vectors.dense(0.3, 0.5, 1)),
      TestDataFrameRow(1.0, 2.0, Vectors.dense(4.0, 5.0, 6.0), 1, 1, 0.4, 0.0, 0.2,
        Seq("a", "b", "c", "d", "d", "d").toArray, Vectors.dense(0.2, 0.1, 0.5)),
      TestDataFrameRow(1.0, 0.0, Vectors.dense(16.0, 11.0, 9.0), 2, 3, 0.4, 1.0, 0.8,
        Seq("a", "c", "d", "f", "f", "g").toArray, Vectors.dense(0.2, 0.1, 1)),
      TestDataFrameRow(0.0, 1.0, Vectors.dense(32.0, 11.0, 9.0), 4, 3, 0.2, 0.0, 0.1,
        Seq("b", "d", "d", "f", "f", "g").toArray, Vectors.dense(0.1, 0.2, 0.1))
    )
    createDataFrame(rowSeq)
  }

  def assertTransformedDF(dataFrame: DataFrame): Unit = {}
  def assertTransformedSchema(schema: StructType): Unit = {}
  def isAlgorithmDeterministic: Boolean = true

  className should {
    "successfully run _fit(), _transform() and _transformSchema()" in {
      val estimatorWithParams = estimator.set(estimatorParams: _*)
      val transformer = estimatorWithParams._fit(executionContext, dataFrame)
      val transformed = transformer._transform(executionContext, dataFrame)
      assertTransformedDF(transformed)
      val transformedSchema = transformer._transformSchema(dataFrame.sparkDataFrame.schema)
      assertTransformedSchema(transformedSchema.get)
      testSerializedTransformer(transformer, transformed, transformedSchema.get)
    }
    "successfully run _fit_infer() and _transformSchema() with schema" in {
      val estimatorWithParams = estimator.set(estimatorParams: _*)
      val transformer = estimatorWithParams._fit_infer(Some(dataFrame.sparkDataFrame.schema))
      transformer._transformSchema(dataFrame.sparkDataFrame.schema)
    }
    "successfully run _fit_infer() without schema" in {
      val estimatorWithParams = estimator.set(estimatorParams: _*)
      estimatorWithParams._fit_infer(None)
    }
    "successfully run report" in {
      val estimatorWithParams = estimator.set(estimatorParams: _*)
      estimatorWithParams.report()
    }
  }

  def testSerializedTransformer(
    transformer: Transformer,
    expectedDF: DataFrame,
    expectedSchema: StructType): Unit = {

    val (df, Some(schema)) = useSerializedTransformer(transformer, dataFrame)
    assertTransformedDF(df)
    if (isAlgorithmDeterministic) {
      assertDataFramesEqual(expectedDF, df, checkRowOrder = false)
    }
    assertTransformedSchema(schema)
    assertSchemaEqual(schema, expectedSchema)
  }

  def useSerializedTransformer(
    transformer: Transformer,
    dataFrame: DataFrame): (DataFrame, Option[StructType]) = {

    transformer.paramValuesToJson
    checkTransformerCorrectness(transformer)
    val deserialized = transformer.loadSerializedTransformer(tempDir)
    val resultDF = deserialized.transform.apply(executionContext)(())(dataFrame)
    val resultSchema = deserialized._transformSchema(dataFrame.sparkDataFrame.schema)
    checkTransformerCorrectness(deserialized)
    (resultDF, resultSchema)
  }

  /**
    * Checks correctness of the transformer e.g.
    * report generation (crucial thing in our system) etc.
    * @param transformer
    */
  private def checkTransformerCorrectness(transformer: Transformer): Unit = {
    // Generating reports is one of the most important functionality of our product,
    // when report is generated without error it very often means that everything went fine,
    // and user won't see any errors.
    transformer.report()
  }
}

object AbstractEstimatorModelWrapperSmokeTest {
  case class TestDataFrameRow(
    myLabel: Double,
    myWeight: Double,
    myFeatures: Linalg.Vector,
    myItemId: Int,
    myUserId: Int,
    myRating: Double,
    myCensor: Double,
    myNoZeroLabel: Double,
    myStringFeatures: Array[String],
    myStandardizedFeatures: Linalg.Vector)
}
