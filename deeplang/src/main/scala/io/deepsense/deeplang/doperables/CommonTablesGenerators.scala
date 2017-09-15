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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.linalg.DenseMatrix

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.params.ParamMap
import io.deepsense.reportlib.model._

object CommonTablesGenerators {

  def sparkParams(params: ParamMap): Table = {
    val values = params.toSeq.map(
      pair =>
        List(Some(pair.param.name), Some(pair.value.toString), Some(pair.param.description)))
    Table(
      name = "Parameters",
      description = "Parameters",
      columnNames = Some(List("Parameter", "Value", "Description")),
      columnTypes = List(ColumnType.string, ColumnType.string, ColumnType.string),
      rowNames = None,
      values = values.toList)
  }

  def params(params: ParamMap): Table = {
    val values = params.toSeq.map(
      pair =>
        List(Some(pair.param.name), Some(pair.value.toString), Some(pair.param.description)))
    Table(
      name = "Parameters",
      description = "Parameters",
      columnNames = Some(List("Parameter", "Value", "Description")),
      columnTypes = List(ColumnType.string, ColumnType.string, ColumnType.string),
      rowNames = None,
      values = values.toList)
  }

  def modelSummary(tableEntries: List[SummaryEntry]): Table = {
    val values = tableEntries.map(
      (entry: SummaryEntry) =>
        List(Some(entry.name), Some(entry.value), Some(entry.description)))

    Table(
      name = "Model summary",
      description = "Model summary",
      columnNames = Some(List("Result", "Value", "Description")),
      columnTypes = List(ColumnType.string, ColumnType.string, ColumnType.string),
      rowNames = None,
      values = values)
  }

  def decisionTree(
      weights: Array[Double],
      trees: Array[_ <: {def depth: Int; def numNodes: Int}]): Table = {

    val treesEntries = (trees.indices, weights, trees).zipped.map(
      (index, weight, tree) => {
        List(
          Some(index.toString),
          Some(weight.toString),
          Some(tree.depth.toString),
          Some(tree.numNodes.toString))
      }).toList

    Table(
      name = "Decision Trees",
      description = "Decision trees.",
      columnNames = Some(List("Tree Index", "Weight", "Depth", "Nodes")),
      columnTypes =
        List(ColumnType.numeric, ColumnType.numeric, ColumnType.numeric, ColumnType.numeric),
      rowNames = None,
      values = treesEntries)
  }

  def categoryMaps(maps: Map[Int, Map[Double, Int]]): Table = {
    val values = maps.toList.map(
      tuple => List(Some(tuple._1.toString), Some(tuple._2.toString())))

    Table(
      name = "Category maps",
      description = "Feature value index. Keys are categorical feature indices (column indices). " +
        "Values are maps from original features values to 0-based category indices. " +
        "If a feature is not in this map, it is treated as continuous.",
      columnNames = Some(List("Index", "Map")),
      columnTypes = List(ColumnType.numeric, ColumnType.string),
      rowNames = None,
      values = values)
  }

  def denseMatrix(matrix: DenseMatrix): Table = {
    val (numRows, numCols) = (matrix.numRows, matrix.numCols)
    val values =
      (for (r <- Range(0, numRows)) yield
        (for (c <- Range(0, numCols)) yield {
          val index = if (!matrix.isTransposed) r + numRows*c else c + numCols*r
          Some(matrix.values(index).toString)
        }).toList).toList

    Table(
      name = "Dense Matrix",
      description = "Dense Matrix",
      columnNames = None,
      columnTypes = List.fill(numCols)(ColumnType.numeric),
      rowNames = None,
      values = values)
  }

  case class SummaryEntry(name: String, value: String, description: String)
}
