/**
 * Copyright (c) 2015, CodiLime Inc.
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
