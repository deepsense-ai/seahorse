/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe.types.categorical

/**
 * Mapping of values in categorical column type. Allows finding a value
 * by its id and the id of a value.
 */
case class CategoriesMapping(valueToId: Map[String, Int], idToValue: Map[Int, String]) {
  val values = valueToId.keySet
  val ids = idToValue.keySet
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
    val zippedWithIndex = values.zipWithIndex
    val valueToId = zippedWithIndex.toMap
    val idToValue = zippedWithIndex.map(_.swap).toMap
    CategoriesMapping(valueToId, idToValue)
  }
}

case class IdToIdMapping(map: Map[Int, Int]) {
  def mapId(id: Int) = map(id)
  val isEmpty: Boolean = map.isEmpty
}

case class MergedMapping(otherToFinal: IdToIdMapping, finalMapping: CategoriesMapping)
