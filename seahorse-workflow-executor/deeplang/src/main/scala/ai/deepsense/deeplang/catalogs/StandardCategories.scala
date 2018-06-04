/**
 * Copyright 2018 deepsense.ai (CodiLime, Inc) & Astraea, Inc.
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

package ai.deepsense.deeplang.catalogs

import ai.deepsense.deeplang.DOperationCategories._
import ai.deepsense.deeplang.catalogs.spi.{CatalogRegistrant, CatalogRegistrar}

/** SPI implementation used to register categories with the system. */
class StandardCategories extends CatalogRegistrant {
  override def register(registrar: CatalogRegistrar): Unit = {
    val p = SortPriority.DEFAULT.inSequence(100)
    registrar.registerCategory(IO, p.next())
    registrar.registerCategory(Action, p.next())
    registrar.registerCategory(SetOperation, p.next())
    registrar.registerCategory(Filtering, p.next())
    registrar.registerCategory(UserDefined, p.next())
    registrar.registerCategory(Transformation, p.next())
    registrar.registerCategory(Transformation.Custom, p.next())
    registrar.registerCategory(Transformation.FeatureConversion, p.next())
    registrar.registerCategory(Transformation.FeatureScaling, p.next())
    registrar.registerCategory(Transformation.TextProcessing, p.next())
    registrar.registerCategory(ML, p.next())
    registrar.registerCategory(ML.Regression, p.next())
    registrar.registerCategory(ML.Classification, p.next())
    registrar.registerCategory(ML.Clustering, p.next())
    registrar.registerCategory(ML.FeatureSelection, p.next())
    registrar.registerCategory(ML.DimensionalityReduction, p.next())
    registrar.registerCategory(ML.Recommendation, p.next())
    registrar.registerCategory(ML.ModelEvaluation, p.next())
    registrar.registerCategory(ML.HyperOptimization, p.next())
    registrar.registerCategory(Other, p.next())
  }
}

