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

package io.deepsense.deeplang.doperations

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql
import org.apache.spark.sql.{Column, Row}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMetadata
import io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation2To1, ExecutionContext}

case class Join() extends DOperation2To1[DataFrame, DataFrame, DataFrame] with JoinParams {

  override val name = "Join"

  override val id: Id = "06374446-3138-4cf7-9682-f884990f3a60"

  override val parameters: ParametersSchema = ParametersSchema(
    "join columns" -> joinColumnsParam,
    "left prefix" -> leftTablePrefixParam,
    "right prefix" -> rightTablePrefixParam
  )

  import io.deepsense.deeplang.doperations.Join._

  override protected def _execute(context: ExecutionContext)
                                 (ldf: DataFrame, rdf: DataFrame): DataFrame = {
    logger.debug("Execution of " + this.getClass.getSimpleName + " starts")

    val leftJoinColumnNames = joinColumnsParam.value.get.map(
      schema => ldf.getColumnName(schema.getSingleColumnSelection(leftColumnParamKey).get))
    val rightJoinColumnNames = joinColumnsParam.value.get.map(
      schema => rdf.getColumnName(schema.getSingleColumnSelection(rightColumnParamKey).get))

    var lsdf = ldf.sparkDataFrame
    var rsdf = rdf.sparkDataFrame

    logger.debug("Validate that columns used for joining is not empty")
    if (leftJoinColumnNames.isEmpty) {
      throw ColumnsDoNotExistException(namesToSelections(leftJoinColumnNames), ldf)
    }
    if(rightJoinColumnNames.isEmpty) {
      throw ColumnsDoNotExistException(namesToSelections(rightJoinColumnNames), rdf)
    }

    logger.debug("Validate types of columns used to join two DataFrames")
    leftJoinColumnNames.zip(rightJoinColumnNames).foreach { case (leftCol, rightCol) => {
      DataFrame.assertExpectedColumnType(
        lsdf.schema.apply(leftCol),
        SparkConversions.sparkColumnTypeToColumnType(rsdf.schema.apply(rightCol).dataType))
    }}

    logger.debug("Append prefixes to columns from left table")
    val (newLsdf, renamedLeftColumns) = appendPrefixes(lsdf, leftTablePrefixParam.value)
    lsdf = newLsdf

    logger.debug("Append prefixes to columns from right table")
    val (newRsdf, renamedRightColumns) = appendPrefixes(rsdf, rightTablePrefixParam.value)
    rsdf = newRsdf

    logger.debug("Rename join columns in right DataFrame if they are present in left DataFrame")
    rsdf.columns.foreach { col =>
      if (renamedLeftColumns.valuesIterator.contains(col)) {
        val newCol = rdf.uniqueColumnName(col, "join")
        renamedRightColumns.put(col, newCol)
        rsdf = rsdf.withColumnRenamed(col, newCol)
      }
    }

    val prefixedLeftJoinColumnNames = leftJoinColumnNames.map(renamedLeftColumns(_))
    val prefixedRightJoinColumnNames = rightJoinColumnNames.map(renamedRightColumns(_))

    logger.debug("Change CategoricalMapping in right DataFrame to allow join with left DataFrame")
    val lcm = CategoricalMetadata(lsdf)
    val rcm = CategoricalMetadata(rsdf)
    prefixedLeftJoinColumnNames.zipWithIndex.foreach { case (col, index) =>
      if (lcm.isCategorical(col)) {
        val rightJoinColumnName = prefixedRightJoinColumnNames(index)

        val lMapping = lcm.mapping(col)
        val rMapping = rcm.mapping(rightJoinColumnName)

        val merged = lMapping.mergeWith(rMapping)
        val otherToFinal = merged.otherToFinal

        val columnIndex = rsdf.columns.indexOf(rightJoinColumnName)
        val rddWithFinalMapping = rsdf.map { r =>
          val id = r.getInt(columnIndex)
          val mappedId = otherToFinal.mapId(id)
          Row.fromSeq(r.toSeq.updated(columnIndex, mappedId))
        }
        rsdf = context.sqlContext.createDataFrame(rddWithFinalMapping, rsdf.schema)
      }
    }

    logger.debug("Prepare joining condition")

    val zippedJoinColumns = (leftJoinColumnNames.zip(rightJoinColumnNames).toList: @unchecked)

    def prepareCondition(leftColumn: String, rightColumn: String): Column = {
      columnEqualityCondition(
        renamedLeftColumns(leftColumn),
        lsdf,
        renamedRightColumns(rightColumn),
        rsdf)
    }

    val initialCondition = zippedJoinColumns.head match {
      case (firstLeftColumnName, firstRightColumnName) =>
        prepareCondition(firstLeftColumnName, firstRightColumnName)
    }

    val joinCondition = zippedJoinColumns.tail.foldLeft(initialCondition) {
      (acc, col) => col match {
        case (leftColumnName, rightColumnName) =>
          acc && prepareCondition(leftColumnName, rightColumnName)
      }
    }

    logger.debug("Joining two DataFrames")
    val joinedDataFrame = lsdf.join(rsdf, joinCondition, "left_outer")

    logger.debug("Removing additional columns in right DataFrame")
    val columns = lsdf.columns ++
      rsdf.columns.filter(col => !prefixedRightJoinColumnNames.contains(col))
    assert(columns.nonEmpty)
    val duplicateColumnsRemoved = joinedDataFrame.select(columns.head, columns.tail: _*)

    // NOTE: We perform only LEFT OUTER JOIN, thus we do not need to change CategoricalMetadata
    // in resultDataFrame (CategoricalMetadata for left DataFrame, already there is sufficient)
    val resultDataFrame = context.dataFrameBuilder.buildDataFrame(duplicateColumnsRemoved)
    logger.debug("Execution of " + this.getClass.getSimpleName + " ends")
    resultDataFrame
  }

  private def columnEqualityCondition(
      leftColumnName: String,
      leftSparkDataFrame: sql.DataFrame,
      rightColumnName: String,
      rightSparkDataFrame: sql.DataFrame): Column = {
    leftSparkDataFrame(leftColumnName) === rightSparkDataFrame(rightColumnName)
  }

  private def namesToSelections(columnNames: Traversable[String]): Vector[ColumnSelection] = {
    columnNames.map((name: String) => new NameColumnSelection(Set(name))).toVector
  }

  private def appendPrefixes(dataFrame: sql.DataFrame, prefixParam: Option[String])
    : (sql.DataFrame, mutable.HashMap[String, String]) = {

    val columnNamesMap = new mutable.HashMap[String, String]()
    val prefix = prefixParam.getOrElse("")

    val renamedColumns = dataFrame.schema.fieldNames.map(col => {
      val newCol = prefix + col
      columnNamesMap.put(col, newCol)
      newCol
    })

    (dataFrame.toDF(renamedColumns: _*), columnNamesMap)
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

trait JoinParams {

  import io.deepsense.deeplang.doperations.Join._

  val joinColumnsParam = ParametersSequence(
    "Pairs of columns to join upon",
    required = true,
    predefinedSchema = ParametersSchema(
      leftColumnParamKey -> SingleColumnSelectorParameter(
        "Column from the left table", required = true, portIndex = 0),
      rightColumnParamKey -> SingleColumnSelectorParameter(
        "Column from the right table", required = true, portIndex = 1)
    )
  )

  val leftTablePrefixParam = PrefixBasedColumnCreatorParameter(
    "Prefix for columns of left table",
    default = None,
    required = false)

  val rightTablePrefixParam = PrefixBasedColumnCreatorParameter(
    "Prefix for columns of right table",
    default = None,
    required = false)
}

object Join {

  def apply(joinColumns: Vector[ParametersSchema],
            prefixLeft: Option[String],
            prefixRight: Option[String]): Join = {
    val join = new Join
    join.joinColumnsParam.value = Some(joinColumns)
    join.leftTablePrefixParam.value = prefixLeft
    join.rightTablePrefixParam.value = prefixRight
    join
  }

  val leftColumnParamKey = "left column"
  val rightColumnParamKey = "right column"
}
