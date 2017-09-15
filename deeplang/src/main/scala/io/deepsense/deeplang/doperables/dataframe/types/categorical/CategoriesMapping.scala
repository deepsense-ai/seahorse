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

/**
 * Mapping of values in categorical column type. Allows finding a value
 * by its id and the id of a value.
 */
case class CategoriesMapping(valueToId: Map[String, Int], idToValue: Map[Int, String]) {
  val valueIdPairs = valueToId.toList.sortBy { case (value, _) => value }
  val values = valueIdPairs.map(_._1)
  val ids = valueIdPairs.map(_._2)
  val isEmpty: Boolean = valueToId.isEmpty

  /**
   * Merge this mapping with some other mapping. Creates a mapping for a merged data set.
   * Because ids in 'other' mapping may correspond to different values than in 'this',
   * a new mapping from 'other' to the final mapping is created too.
   * @param other The other mapping that 'this' should be merged with.
   * @return A structure containing both merged final mapping and mapping from
   *         the 'other' to final.
   */
  def mergeWith(other: CategoriesMapping): MergedMapping = {
    val finalMappingBuilder = new MappingBuilder(this)

    val otherToFinal = other.values.map { v =>
      val generatedId = finalMappingBuilder.add(v)
      other.valueToId(v) -> generatedId
    }.toMap

    MergedMapping(IdToIdMapping(otherToFinal), finalMappingBuilder.build)
  }

  private class MappingBuilder(mapping: CategoriesMapping) {
    private var valueToId = mapping.valueToId
    private var idToValue = mapping.idToValue

    def add(value: String): Int = {
      valueToId.getOrElse(value, {
        val nextId = valueToId.size
        valueToId = valueToId.updated(value, nextId)
        idToValue = idToValue.updated(nextId, value)
        nextId.toInt
      })
    }

    def build: CategoriesMapping = CategoriesMapping(valueToId, idToValue)
  }
}

object CategoriesMapping {
  val empty = CategoriesMapping(Map.empty, Map.empty)
  def apply(values: Seq[String]): CategoriesMapping = {
    require(values.forall(_ != null), "Mapping can not contain null")
    val zippedWithIndex = values.zipWithIndex
    val valueToId = zippedWithIndex.toMap
    val idToValue = zippedWithIndex.map(_.swap).toMap
    CategoriesMapping(valueToId, idToValue)
  }
}

case class IdToIdMapping(map: Map[Int, Int]) {
  def mapId(id: Int): Int = map(id)
  val isEmpty: Boolean = map.isEmpty
}

case class MergedMapping(otherToFinal: IdToIdMapping, finalMapping: CategoriesMapping)
