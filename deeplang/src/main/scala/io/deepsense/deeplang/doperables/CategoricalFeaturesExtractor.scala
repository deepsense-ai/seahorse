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

  /**
   * Extracts categorical features information from selected columns of a dataframe.
   *
   * @param dataframe DataFrame
   * @param featureColumns columns to extract number of categories for
   * @return map with key columnIndex and value numberOfCategories
   *         for each non-empty categorical feature
   */
  def extractCategoricalFeatures(
      dataframe: DataFrame, featureColumns: Seq[String]): Map[Int, Int] = {

    val columnMapping = featureColumns.zipWithIndex.toMap
    dataframe.metadata.map { metadata =>
      metadata.columns.values.flatMap {
        case CategoricalColumnMetadata(name, _, Some(categories)) =>
          val categoriesCount = categories.values.length
          // Exclude categorical features with no categories
          if (featureColumns.contains(name) && categoriesCount > 0) {
            Some(columnMapping(name) -> categoriesCount)
          } else {
            None
          }
        case _ => None
      }.toMap
    }.getOrElse(Map.empty)
  }
}
