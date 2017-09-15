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

package io.deepsense.deeplang.doperables.dataframe.types.categorical

import scala.util.Try

import org.apache.spark.sql.types.{MetadataBuilder, Metadata}

trait MappingMetadataConverter {

  /**
   * Reads categories mapping from field's metadata. Requires metadata to contain sub-metadata
   * under a key 'categorical'.
   * @param metadata Metadata of a field.
   * @return Mapping or None when the metadata does not contain
   *         categories mapping information.
   */
  def mappingFromMetadata(metadata: Metadata): Option[CategoriesMapping] =
    Try {
      val categoricalMetadata = metadata.getMetadata(MappingMetadataConverter.CategoricalKey)
      val values = categoricalMetadata.getStringArray(MappingMetadataConverter.ValuesKey).toSet
      val valueIdPairs = values.map(v => v -> categoricalMetadata.getLong(v).toInt)
      val valueToId = valueIdPairs.toMap
      val idToValue = valueIdPairs.map { case (v, id) => id -> v }.toMap
      CategoriesMapping(valueToId, idToValue)
    }.toOption

  /**
   * Adds mapping to metadata object (by creating a new metadata object).
   * Stores the mapping under a key 'categorical'. Overwrites previous version
   * of the mapping in the metadata object (if any).
   * @param mapping Mapping to be writen to metadata.
   * @param metadata Metadata object to modify.
   * @return New metadata object (a copy of the input metadata object) with mapping saved
   *         under a key 'categorical'.
   */
  def mappingToMetadata(mapping: CategoriesMapping, metadata: Metadata): Metadata =
    new MetadataBuilder()
      .withMetadata(metadata)
      .putMetadata(MappingMetadataConverter.CategoricalKey, mappingToFlatMetadata(mapping))
      .build()

  /**
   * @param mapping Mapping to be written to metadata.
   * @return New metadata object (a copy of the input metadata object) with mapping saved
   *         under a key 'categorical'. Previous version of the mapping in the metadata object
   *         (if any) are overwritten.
   */
  def mappingToMetadata(mapping: CategoriesMapping): Metadata =
    new MetadataBuilder()
      .putMetadata(MappingMetadataConverter.CategoricalKey, mappingToFlatMetadata(mapping))
      .build()

  /**
   * Translates a mapping to a flat metadata object.
   * @param mapping Mapping to be translated
   * @return Metadata object with mapping description.
   */
  private def mappingToFlatMetadata(mapping: CategoriesMapping): Metadata = {
    val metadataBuilder = new MetadataBuilder()
      .putStringArray(MappingMetadataConverter.ValuesKey, mapping.values.toArray)
    mapping.values
      .foldLeft(metadataBuilder){ case (b, v) => b.putLong(v, mapping.valueToId(v)) }
      .build()
  }
}

object MappingMetadataConverter extends MappingMetadataConverter {
  val CategoricalKey = "categorical"
  val ValuesKey = "values"
}

