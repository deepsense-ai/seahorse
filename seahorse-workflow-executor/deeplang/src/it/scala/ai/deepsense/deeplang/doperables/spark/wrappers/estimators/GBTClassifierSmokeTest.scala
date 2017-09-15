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

import org.apache.spark.sql.types.{DoubleType, Metadata, StructType}

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.ClassificationImpurity
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection
import ai.deepsense.deeplang.utils.DataFrameUtils

class GBTClassifierSmokeTest
  extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "GBTClassifier"

  override val estimator = new GBTClassifier()

  private val labelColumnName = "myRating"

  import estimator.vanillaGBTClassifier._

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    impurity -> ClassificationImpurity.Entropy(),
    labelColumn -> NameSingleColumnSelection(labelColumnName),
    lossType -> GBTClassifier.Logistic(),
    maxBins -> 2.0,
    maxDepth -> 6.0,
    maxIterations -> 10.0,
    minInfoGain -> 0.0,
    minInstancesPerNode -> 1,
    predictionColumn -> "prediction",
    seed -> 100.0,
    stepSize -> 0.11,
    subsamplingRate -> 0.999
  )

  override def assertTransformedDF(dataFrame: DataFrame): Unit = {
    val possibleValues = DataFrameUtils.collectValues(dataFrame, labelColumnName)
    val actualValues = DataFrameUtils.collectValues(dataFrame, "prediction")

    actualValues.diff(possibleValues) shouldBe empty
  }

  override def assertTransformedSchema(schema: StructType): Unit = {
    val predictionColumn = schema.fields.last
    predictionColumn.name shouldBe "prediction"
    predictionColumn.dataType shouldBe DoubleType
    predictionColumn.metadata shouldBe Metadata.empty
  }
}
