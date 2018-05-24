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

package ai.deepsense.deeplang.doperables.spark.wrappers.models

import scala.language.reflectiveCalls

import org.apache.spark.ml.feature.{PCA => SparkPCA, PCAModel => SparkPCAModel}

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.SparkSingleColumnModelWrapper
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.serialization.SerializableSparkModel
import ai.deepsense.deeplang.params.Param
import ai.deepsense.sparkutils.ML

class PCAModel
  extends SparkSingleColumnModelWrapper[SparkPCAModel, SparkPCA] {

  override protected def getSpecificParams: Array[Param[_]] = Array()

  override def report(extended: Boolean = true): Report = {
    super.report(extended)
      .withAdditionalTable(CommonTablesGenerators.denseMatrix(
        name = "A Principal Components Matrix",
        description = "Each column is one principal component.",
        matrix = ML.ModelParams.pcFromPCAModel(sparkModel)))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkPCAModel] = {
    new SerializableSparkModel(SparkPCAModel.load(path))
  }
}
