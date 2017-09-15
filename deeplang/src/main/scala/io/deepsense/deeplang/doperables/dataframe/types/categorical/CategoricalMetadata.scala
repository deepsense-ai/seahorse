/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe.types.categorical

case class CategoricalMetadata(sparkDataFrame: org.apache.spark.sql.DataFrame) {
  import MappingMetadataConverter._
  private val schema = sparkDataFrame.schema
  private val mappingTriplets = schema.fieldNames.zipWithIndex.flatMap {
    case (name, index) =>
      val fieldMetadata = schema.apply(index).metadata
      mappingFromMetadata(fieldMetadata).map(m => (name, index, m))
  }

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
