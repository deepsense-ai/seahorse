/**
 * Copyright 2015, CodiLime Inc.
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

import io.deepsense.deeplang.doperables.dataframe.{CategoricalColumnMetadata, DataFrame}

trait CategoricalFeaturesExtractor {

  def extractCategoricalFeatures(dataframe: DataFrame): Map[Int, Int] = {
    dataframe.metadata.map { metadata =>
      metadata.columns.values.map {
        case CategoricalColumnMetadata(_, Some(index), Some(categories)) =>
          index -> categories.values.length
        case _ => null
      }.filter(_ != null).toMap
    }.getOrElse(Map.empty)
  }
}
