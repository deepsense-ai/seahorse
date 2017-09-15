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

import io.deepsense.deeplang.catalogs.doperations.DOperationCategory

object DOperationCategories {

  import io.deepsense.commons.models.Id._

  object IO extends DOperationCategory("5a39e324-15f4-464c-83a5-2d7fba2858aa", "Input/Output")

  object Action extends DOperationCategory("f0202a40-7fe7-4d11-bfda-b11b2199cc12", "Action")

  object SetOperation
    extends DOperationCategory("6c730c11-9708-4a84-9dbd-3845903f32ac", "Set operation")

  object Filtering extends DOperationCategory("a6114fc2-3144-4828-b350-4232d0d32f91", "Filtering")

  object Transformation
    extends DOperationCategory("3fcc6ce8-11df-433f-8db3-fa1dcc545ed8", "Transformation") {

    object Custom extends DOperationCategory(
        "c866200b-9b7e-49d8-8582-d182593629a2", "Custom", Transformation) {

      object SQL
        extends DOperationCategory(
          "9cf8dadb-aadf-4ef3-8ede-fee31bc49d8f", "SQL", Custom)

      object Python
        extends DOperationCategory(
          "b68164ef-253c-4e70-9b18-0316453ea61d", "Python", Custom)
    }

    object FeatureConversion extends DOperationCategory(
      "6d84c023-a5f9-4713-8707-1db2c94ccd09", "Feature conversion", Transformation)

    object FeatureScaling extends DOperationCategory(
      "da9ec3ca-d3ba-4fca-ad22-7298b725d747", "Feature scaling", Transformation)

    object TextProcessing extends DOperationCategory(
      "abfc2e76-e2b7-46ad-8fc2-4f80af421432", "Text processing", Transformation)
  }

  object ML extends DOperationCategory("c730c11-9708-4a84-9dbd-3845903f32ac", "Machine learning") {

    object Regression
      extends DOperationCategory("c80397a8-7840-4bdb-83b3-dc12f1f5bc3c", "Regression", ML)

    object Classification
      extends DOperationCategory("ff13cbbd-f4ec-4df3-b0c3-f6fd4b019edf", "Classification", ML)

    object Clustering
      extends DOperationCategory("5d6ed17f-7dc5-4b50-954c-8b2bbe6da2fd", "Clustering", ML)

    object FeatureSelection extends DOperationCategory(
      "e6b28974-d2da-4615-b357-bc6055238cff", "Feature selection", ML)

    object DimensionalityReduction
      extends DOperationCategory(
        "a112511e-5433-4ed2-a675-098a14a63c00", "Dimensionality reduction", ML)

    object Recommendation
      extends DOperationCategory("daf4586c-4107-4aab-bfab-2fe4e1652784", "Recommendation", ML)

    object ModelEvaluation
      extends DOperationCategory("b5d34823-3f2c-4a9a-9114-3c126ce8dfb6", "Model evaluation", ML)

    object HyperOptimization
      extends DOperationCategory("5a26f196-4805-4d8e-9a8b-b4c5c4538b0b", "Hyper Optimization", ML)
  }
}
