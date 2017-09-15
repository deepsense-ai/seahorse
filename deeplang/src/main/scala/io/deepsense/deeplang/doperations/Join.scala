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

package io.deepsense.deeplang.doperations

import scala.collection.mutable
import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql
import org.apache.spark.sql.Column
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.{DataFrameColumnsGetter, DataFrame}
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.params._
import io.deepsense.deeplang.params.selections.{ColumnSelection, NameColumnSelection, SingleColumnSelection}
import io.deepsense.deeplang.{DKnowledge, DOperation2To1, ExecutionContext}

case class Join()
    extends DOperation2To1[DataFrame, DataFrame, DataFrame]
    with Params {

  import Join._

  override val name = "Join"
  override val id: Id = "06374446-3138-4cf7-9682-f884990f3a60"
  override val description: String =
    "Joins two DataFrames to a DataFrame"

  val leftPrefix = PrefixBasedColumnCreatorParam(
    name = "left prefix",
    description = "Prefix for columns of left DataFrame")
  setDefault(leftPrefix, "")

  def getLeftPrefix: String = $(leftPrefix)
  def setLeftPrefix(value: String): this.type = set(leftPrefix, value)

  val rightPrefix = PrefixBasedColumnCreatorParam(
    name = "right prefix",
    description = "Prefix for columns of right DataFrame")
  setDefault(rightPrefix, "")

  def getRightPrefix: String = $(rightPrefix)
  def setRightPrefix(value: String): this.type = set(rightPrefix, value)

  val joinColumns = ParamsSequence[ColumnPair](
    name = "join columns",
    description = "Pairs of columns to join upon")

  def getJoinColumns: Seq[ColumnPair] = $(joinColumns)
  def setJoinColumns(value: Seq[ColumnPair]): this.type = set(joinColumns, value)

  val params = declareParams(leftPrefix, rightPrefix, joinColumns)

  override protected def _execute(context: ExecutionContext)
                                 (ldf: DataFrame, rdf: DataFrame): DataFrame = {
    logger.debug("Execution of " + this.getClass.getSimpleName + " starts")

    val (leftJoinColumnNames, rightJoinColumnNames) = validateSchemas(
      ldf.sparkDataFrame.schema, rdf.sparkDataFrame.schema)

    var lsdf = ldf.sparkDataFrame
    var rsdf = rdf.sparkDataFrame

    logger.debug("Append prefixes to columns from left table")
    val (newLsdf, renamedLeftColumns) = appendPrefixes(lsdf, getLeftPrefix)
    lsdf = newLsdf

    logger.debug("Append prefixes to columns from right table")
    val (newRsdf, renamedRightColumns) = appendPrefixes(rsdf, getRightPrefix)
    rsdf = newRsdf

    logger.debug("Rename join columns in right DataFrame if they are present in left DataFrame")
    rsdf.columns.foreach { col =>
      if (renamedLeftColumns.valuesIterator.contains(col)) {
        val newCol = DataFrameColumnsGetter.uniqueColumnName(rdf.sparkDataFrame.schema, col, "join")
        renamedRightColumns.put(col, newCol)
        rsdf = rsdf.withColumnRenamed(col, newCol)
      }
    }

    val prefixedLeftJoinColumnNames = leftJoinColumnNames.map(renamedLeftColumns(_))
    val prefixedRightJoinColumnNames = rightJoinColumnNames.map(renamedRightColumns(_))

    logger.debug("Prepare joining condition")

    val joinCondition: Column = prepareJoiningCondition(
      leftJoinColumnNames, rightJoinColumnNames,
      lsdf, rsdf, renamedLeftColumns, renamedRightColumns)

    logger.debug("Joining two DataFrames")
    val joinedDataFrame = lsdf.join(rsdf, joinCondition, "left_outer")

    logger.debug("Removing additional columns in right DataFrame")
    val columns = lsdf.columns ++
      rsdf.columns.filter(col => !prefixedRightJoinColumnNames.contains(col))
    assert(columns.nonEmpty)
    val duplicateColumnsRemoved = joinedDataFrame.select(columns.head, columns.tail: _*)

    val resultDataFrame = DataFrame.fromSparkDataFrame(duplicateColumnsRemoved)
    logger.debug("Execution of " + this.getClass.getSimpleName + " ends")
    resultDataFrame
  }

  override protected def _inferKnowledge(
      context: InferContext)(
      leftDataFrameKnowledge: DKnowledge[DataFrame],
      rightDataFrameKnowledge: DKnowledge[DataFrame])
    : (DKnowledge[DataFrame], InferenceWarnings) = {

    val leftSchema = leftDataFrameKnowledge.single.schema
    val rightSchema = rightDataFrameKnowledge.single.schema

    if (leftSchema.isDefined && rightSchema.isDefined) {
      val outputSchema = inferSchema(leftSchema.get, rightSchema.get)
      (DKnowledge(DataFrame.forInference(outputSchema)), InferenceWarnings.empty)
    } else {
      (DKnowledge(DataFrame.forInference()), InferenceWarnings.empty)
    }
  }

  private def inferSchema(leftSchema: StructType, rightSchema: StructType): StructType = {
    val (leftJoinColumnNames, rightJoinColumnNames) = validateSchemas(leftSchema, rightSchema)

    val renamedLeftColumns = appendPrefixes(leftSchema, getLeftPrefix)
    val renamedRightColumns = appendPrefixes(rightSchema, getRightPrefix)

    var prefixedLeftSchema = StructType(
      leftSchema.map(field => field.copy(name = renamedLeftColumns(field.name))))
    var prefixedRightSchema = StructType(
      rightSchema.map(field => field.copy(name = renamedRightColumns(field.name))))

    logger.debug("Rename join columns in right DataFrame if they are present in left DataFrame")
    prefixedRightSchema.foreach { col =>
      if (renamedLeftColumns.valuesIterator.contains(col.name)) {
        val newCol = DataFrameColumnsGetter.uniqueColumnName(rightSchema, col.name, "join")
        renamedRightColumns.put(col.name, newCol)

        prefixedRightSchema = StructType(prefixedRightSchema.updated(
          prefixedRightSchema.fieldIndex(col.name),
          prefixedRightSchema.apply(col.name).copy(name = newCol)))
      }
    }

    val prefixedLeftJoinColumnNames = leftJoinColumnNames.map(renamedLeftColumns(_))
    val prefixedRightJoinColumnNames = rightJoinColumnNames.map(renamedRightColumns(_))

    val columns = prefixedLeftSchema ++
      prefixedRightSchema.filter(col => !prefixedRightJoinColumnNames.contains(col.name))
    StructType(columns)
  }

  private def prepareJoiningCondition(
      leftJoinColumnNames: Seq[String],
      rightJoinColumnNames: Seq[String],
      lsdf: sql.DataFrame,
      rsdf: sql.DataFrame,
      renamedLeftColumns: mutable.HashMap[String, String],
      renamedRightColumns: mutable.HashMap[String, String]): Column = {
    val zippedJoinColumns = leftJoinColumnNames.zip(rightJoinColumnNames).toList

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

    joinCondition
  }

  private def validateSchemas(
      leftSchema: StructType,
      rightSchema: StructType): (Seq[String], Seq[String]) = {

    val leftJoinColumnNames = getJoinColumns.map(columnPair =>
      DataFrameColumnsGetter.getColumnName(leftSchema, columnPair.getLeftColumn))
    val rightJoinColumnNames = getJoinColumns.map(columnPair =>
      DataFrameColumnsGetter.getColumnName(rightSchema, columnPair.getRightColumn))

    logger.debug("Validate that columns used for joining is not empty")
    if (leftJoinColumnNames.isEmpty) {
      throw ColumnsDoNotExistException(namesToSelections(leftJoinColumnNames), leftSchema)
    }
    if (rightJoinColumnNames.isEmpty) {
      throw ColumnsDoNotExistException(namesToSelections(rightJoinColumnNames), rightSchema)
    }

    logger.debug("Validate types of columns used to join two DataFrames")
    leftJoinColumnNames.zip(rightJoinColumnNames).foreach { case (leftCol, rightCol) =>
      DataFrame.assertExpectedColumnType(
        leftSchema.apply(leftCol),
        SparkConversions.sparkColumnTypeToColumnType(rightSchema.apply(rightCol).dataType))
    }

    (leftJoinColumnNames, rightJoinColumnNames)
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

  private def appendPrefixes(dataFrame: sql.DataFrame, prefixParam: String)
    : (sql.DataFrame, mutable.HashMap[String, String]) = {

    val columnNamesMap = new mutable.HashMap[String, String]()

    val renamedColumns = dataFrame.schema.fieldNames.map(col => {
      val newCol = prefixParam + col
      columnNamesMap.put(col, newCol)
      newCol
    })

    (dataFrame.toDF(renamedColumns: _*), columnNamesMap)
  }

  private def appendPrefixes(
      schema: StructType, prefixParam: String): mutable.HashMap[String, String] = {
    val columnNamesMap = new mutable.HashMap[String, String]()

    val renamedColumns = schema.fieldNames.map(col => {
      val newCol = prefixParam + col
      columnNamesMap.put(col, newCol)
      newCol
    })

    columnNamesMap
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object Join {

  case class ColumnPair() extends Params {

    val leftColumn = SingleColumnSelectorParam(
      name = "left column",
      description = "Column from the left DataFrame",
      portIndex = 0)

    def getLeftColumn: SingleColumnSelection = $(leftColumn)
    def setLeftColumn(value: SingleColumnSelection): this.type = set(leftColumn, value)

    val rightColumn = SingleColumnSelectorParam(
      name = "right column",
      description = "Column from the left DataFrame",
      portIndex = 1)

    def getRightColumn: SingleColumnSelection = $(rightColumn)
    def setRightColumn(value: SingleColumnSelection): this.type = set(rightColumn, value)

    val params = declareParams(leftColumn, rightColumn)
  }
}
