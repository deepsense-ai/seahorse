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

package io.deepsense.deeplang.doperables.dataframe.types.categorical

import io.deepsense.deeplang.doperables.dataframe.DataFrame

case class CategoricalMetadata(sparkDataFrame: org.apache.spark.sql.DataFrame) {
  import io.deepsense.deeplang.doperables.dataframe.types.categorical.MappingMetadataConverter._
  private val schema = sparkDataFrame.schema
  private val mappingTriplets = for {
      (field, index) <- schema.zipWithIndex
      mapping <- mappingFromMetadata(field.metadata)
    } yield (field.name, index, mapping)

  val mappingById =
    mappingTriplets.map { case (_, index, mapping) => index -> mapping }.toMap
  val mappingByName =
    mappingTriplets.map { case (name, _, mapping) => name -> mapping }.toMap

  def mapping(id: Int): CategoriesMapping = mappingById(id)
  def mapping(name: String): CategoriesMapping = mappingByName(name)
  def mappingOptional(id: Int): Option[CategoriesMapping] = mappingById.get(id)
  def mappingOptional(name: String): Option[CategoriesMapping] = mappingByName.get(name)
  def isCategorical(id: Int): Boolean = mappingById.contains(id)
  def isCategorical(name: String): Boolean = mappingByName.contains(name)
}

object CategoricalMetadata {
  def apply(dataFrame: DataFrame): CategoricalMetadata =
    CategoricalMetadata(dataFrame.sparkDataFrame)
}
