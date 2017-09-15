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

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql
import org.apache.spark.sql.Column
import org.apache.spark.sql.types.StructType

import io.deepsense.commons.types.SparkConversions
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.{DataFrameColumnsGetter, DataFrame}
import io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.params._
import io.deepsense.deeplang.params.selections.{ColumnSelection, NameColumnSelection, SingleColumnSelection}
import io.deepsense.deeplang.{DKnowledge, DOperation2To1, ExecutionContext}

case class Join()
    extends DOperation2To1[DataFrame, DataFrame, DataFrame]
    with Params {

  import Join._

  override val id: Id = "06374446-3138-4cf7-9682-f884990f3a60"
  override val name = "Join"
  override val description: String =
    "Joins two DataFrames to a DataFrame"

  val leftPrefix = PrefixBasedColumnCreatorParam(
    name = "left prefix",
    description = "Prefix for columns of left DataFrame.")
  setDefault(leftPrefix, "")

  def getLeftPrefix: String = $(leftPrefix)
  def setLeftPrefix(value: String): this.type = set(leftPrefix, value)

  val rightPrefix = PrefixBasedColumnCreatorParam(
    name = "right prefix",
    description = "Prefix for columns of right DataFrame.")
  setDefault(rightPrefix, "")

  def getRightPrefix: String = $(rightPrefix)
  def setRightPrefix(value: String): this.type = set(rightPrefix, value)

  val joinColumns = ParamsSequence[ColumnPair](
    name = "join columns",
    description = "Pairs of columns to join upon.")

  def getJoinColumns: Seq[ColumnPair] = $(joinColumns)
  def setJoinColumns(value: Seq[ColumnPair]): this.type = set(joinColumns, value)

  val params = declareParams(leftPrefix, rightPrefix, joinColumns)

  override protected def _execute(context: ExecutionContext)
      (ldf: DataFrame, rdf: DataFrame): DataFrame = {

    logger.debug("Execution of " + this.getClass.getSimpleName + " starts")

    validateSchemas(ldf.sparkDataFrame.schema, rdf.sparkDataFrame.schema)

    val columnNames = RenamedColumnNames(ldf.sparkDataFrame.columns, rdf.sparkDataFrame.columns)
    val leftColumnNames = getSelectedJoinColumsNames(
        ldf.sparkDataFrame.schema, _.getLeftColumn)
    val rightColumnNames = getSelectedJoinColumsNames(
        rdf.sparkDataFrame.schema, _.getRightColumn)
    val joinColumnsPairs = leftColumnNames.zip(rightColumnNames)

    val renamedJoinColumnsPairs =
      joinColumnsPairs.map {
        case (leftColumn, rightColumn) =>
          val renamedLeft = columnNames.left.originalToRenamed(leftColumn)
          val renamedRight = columnNames.right.originalToRenamed(rightColumn)
          (renamedLeft, renamedRight)
      }

    val lsdf = sparkDFWithColumnsRenamed(
      ldf.sparkDataFrame,
      columnNames.left.original,
      columnNames.left.renamed)

    val rsdf = sparkDFWithColumnsRenamed(
      rdf.sparkDataFrame,
      columnNames.right.original,
      columnNames.right.renamed)

    logger.debug("Prepare joining condition")
    val joinCondition = prepareJoiningCondition(renamedJoinColumnsPairs, lsdf, rsdf)

    logger.debug("Joining two DataFrames")
    val joinedDataFrame = lsdf.join(rsdf, joinCondition, Type.leftOuter)

    logger.debug("Removing additional columns in right DataFrame")
    val noDuplicatesSparkDF = sparkDFWithRemovedDuplicatedColumns(
      joinedDataFrame,
      renamedJoinColumnsPairs,
      lsdf.columns,
      rsdf.columns)

    DataFrame.fromSparkDataFrame(noDuplicatesSparkDF)
  }

  override protected def _inferKnowledge(context: InferContext)
      (leftDataFrameKnowledge: DKnowledge[DataFrame],
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

  private def inferSchema(
      leftSchema: StructType,
      rightSchema: StructType): StructType = {

    validateSchemas(leftSchema, rightSchema)
    val columnNames = RenamedColumnNames(leftSchema.fieldNames, rightSchema.fieldNames)

    val prefixedLeftSchema = StructType(
      leftSchema.map(
        field => field.copy(name = columnNames.left.originalToRenamed(field.name))))
    val prefixedRightSchema = StructType(
      rightSchema.map(
        field => field.copy(name = columnNames.right.originalToRenamed(field.name))))
    val rightJoinColumnNames = getSelectedJoinColumsNames(
        rightSchema, _.getRightColumn)
    val renamedRightJoinColumnNames =
      rightJoinColumnNames.map(columnNames.right.originalToRenamed(_))

    val columns = prefixedLeftSchema ++
      prefixedRightSchema.filter(col => !renamedRightJoinColumnNames.contains(col.name))

    StructType(columns)
  }


  private def sparkDFWithColumnsRenamed(
      initSdf: sql.DataFrame,
      colFrom: Seq[String],
      colTo: Seq[String]): sql.DataFrame = {
    val zipped = colFrom zip colTo
    zipped.foldLeft(initSdf){
      (sdf, pair) =>
        val (from, to) = pair
        sdf.withColumnRenamed(from, to)
    }
  }

  private def sparkDFWithRemovedDuplicatedColumns(
      joinedDataFrame: sql.DataFrame,
      renamedJoinColumnsPairs: Seq[(String, String)],
      renamedLeftColumns: Seq[String],
      renamedRightColumns: Seq[String]): sql.DataFrame = {
    val (_, rightJoinColumnNames) = renamedJoinColumnsPairs.unzip
    val columns = renamedLeftColumns ++ renamedRightColumns.filter(
      col => !rightJoinColumnNames.contains(col))
    assert(columns.nonEmpty)
    joinedDataFrame.select(columns.head, columns.tail: _*)
  }

  private def getSelectedJoinColumsNames(
      schema: StructType,
      selector: ColumnPair => SingleColumnSelection): Seq[String] = {
    getJoinColumns.map(columnPair =>
      DataFrameColumnsGetter.getColumnName(schema, selector(columnPair)))
  }
  private def validateSchemas(
      leftSchema: StructType,
      rightSchema: StructType): Unit = {

    val leftJoinColumnNames = getSelectedJoinColumsNames(
        leftSchema, _.getLeftColumn)
    val rightJoinColumnNames = getSelectedJoinColumsNames(
        rightSchema, _.getRightColumn)

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
  }

  private def prepareJoiningCondition(
      joinColumns: Seq[(String, String)],
      lsdf: sql.DataFrame,
      rsdf: sql.DataFrame): Column = {
    require(joinColumns.nonEmpty)
    val initialCondition = joinColumns.head match {
      case (leftHead, rightHead) =>
        lsdf(leftHead) === rsdf(rightHead)
    }
    val joinCondition = joinColumns.foldLeft(initialCondition) {
      case (acc, (leftColumnName, rightColumnName)) =>
        acc && (lsdf(leftColumnName) === rsdf(rightColumnName))
    }
    joinCondition
  }

  private def namesToSelections(columnNames: Traversable[String]): Vector[ColumnSelection] = {
    columnNames.map((name: String) => new NameColumnSelection(Set(name))).toVector
  }


  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

  private case class RenamedColumnNames(
      originalLeftColumns: Seq[String],
      originalRightColumns: Seq[String]) {

    val left = LeftColumnNames(originalLeftColumns, getLeftPrefix)
    val right = RightColumnNames(originalRightColumns, getRightPrefix, left.prefixed)

    abstract class ColumnNames(original: Seq[String], prefix: String) {
      val prefixed = original.map(col => prefix + col)
      val renamed: Seq[String]
      lazy val originalToRenamed: Map[String, String] = original.zip(renamed).toMap
      lazy val renamedToOriginal: Map[String, String] = renamed.zip(original).toMap
    }

    case class LeftColumnNames(
        original: Seq[String],
        prefix: String)
      extends ColumnNames(original, prefix) {
      override val renamed = prefixed
    }

    case class RightColumnNames(
        original: Seq[String],
        prefix: String,
        leftPrefixed: Seq[String])
      extends ColumnNames(original, prefix) {

      val duplicatedPrefixedColumns = prefixed.toSet intersect leftPrefixed.toSet
      val renamed = prefixed.map(
        col => {
          if (duplicatedPrefixedColumns contains col) {
            DataFrameColumnsGetter.uniqueSuffixedColumnName(col)
          } else {
            col
          }
        }
      )
    }
  }
}

object Join {
  object Type {
    val (inner, outer, leftOuter, rightOuter, leftSemi) =
      ("inner", "outer", "left_outer", "right_outer", "leftsemi")
  }

  case class ColumnPair() extends Params {

    val leftColumn = SingleColumnSelectorParam(
      name = "left column",
      description = "Column from the left DataFrame.",
      portIndex = 0)

    def getLeftColumn: SingleColumnSelection = $(leftColumn)
    def setLeftColumn(value: SingleColumnSelection): this.type = set(leftColumn, value)

    val rightColumn = SingleColumnSelectorParam(
      name = "right column",
      description = "Column from the right DataFrame.",
      portIndex = 1)

    def getRightColumn: SingleColumnSelection = $(rightColumn)
    def setRightColumn(value: SingleColumnSelection): this.type = set(rightColumn, value)

    val params = declareParams(leftColumn, rightColumn)
  }
}
