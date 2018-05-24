/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql
import org.apache.spark.sql.Column
import org.apache.spark.sql.types.StructType

import ai.deepsense.commons.types.SparkConversions
import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameColumnsGetter}
import ai.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import ai.deepsense.deeplang.inference.InferenceWarnings
import ai.deepsense.deeplang.params._
import ai.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import ai.deepsense.deeplang.params.selections.{NameColumnSelection, SingleColumnSelection}
import ai.deepsense.deeplang.{DOperation2To1, DataFrame2To1Operation, ExecutionContext}

case class Join()
  extends DOperation2To1[DataFrame, DataFrame, DataFrame]
  with DataFrame2To1Operation
  with Params
  with OperationDocumentation {

  import Join._

  override val id: Id = "06374446-3138-4cf7-9682-f884990f3a60"
  override val name = "Join"
  override val description: String =
    "Joins two DataFrames to a DataFrame"

  override val since: Version = Version(0, 4, 0)

  val joinType = ChoiceParam[JoinTypeChoice.Option](
    name = "join type",
    description = Some("Type of join operation."))
  setDefault(joinType, JoinTypeChoice.Inner())

  def getJoinType: JoinTypeChoice.Option = $(joinType)
  def setJoinType(value: JoinTypeChoice.Option): this.type = set(joinType, value)

  val leftPrefix = new PrefixBasedColumnCreatorParam(
    name = "left prefix",
    description = Some("Prefix for columns of left DataFrame.")
  ) with EmptyPrefixValidator
  setDefault(leftPrefix, "")

  def getLeftPrefix: String = $(leftPrefix)
  def setLeftPrefix(value: String): this.type = set(leftPrefix, value)

  val rightPrefix = new PrefixBasedColumnCreatorParam(
    name = "right prefix",
    description = Some("Prefix for columns of right DataFrame.")
  ) with EmptyPrefixValidator
  setDefault(rightPrefix, "")

  def getRightPrefix: String = $(rightPrefix)
  def setRightPrefix(value: String): this.type = set(rightPrefix, value)

  val joinColumns = ParamsSequence[ColumnPair](
    name = "join columns",
    description = Some("Pairs of columns to join upon."))

  def getJoinColumns: Seq[ColumnPair] = $(joinColumns)
  def setJoinColumns(value: Seq[ColumnPair]): this.type = set(joinColumns, value)

  val specificParams: Array[ai.deepsense.deeplang.params.Param[_]] = Array(
    joinType,
    leftPrefix,
    rightPrefix,
    joinColumns)

  override protected def execute(ldf: DataFrame, rdf: DataFrame)(context: ExecutionContext): DataFrame = {

    logger.debug("Execution of " + this.getClass.getSimpleName + " starts")

    validateSchemas(ldf.sparkDataFrame.schema, rdf.sparkDataFrame.schema)
    val joinType = getJoinType
    val columnNames = RenamedColumnNames(ldf.sparkDataFrame.columns, rdf.sparkDataFrame.columns)
    val leftColumnNames = getSelectedJoinColumnsNames(
        ldf.sparkDataFrame.schema, _.getLeftColumn)
    val rightColumnNames = getSelectedJoinColumnsNames(
        rdf.sparkDataFrame.schema, _.getRightColumn)
    val joinColumnsPairs = leftColumnNames.zip(rightColumnNames)

    val lsdf = sparkDFWithColumnsRenamed(
      ldf.sparkDataFrame,
      columnNames.left.original,
      columnNames.left.renamed)

    val rsdf = sparkDFWithColumnsRenamed(
      rdf.sparkDataFrame,
      columnNames.right.original,
      columnNames.right.renamed)

    val renamedJoinColumnsPairs =
      joinColumnsPairs.map {
        case (leftColumn, rightColumn) =>
          val columnLeft = lsdf(columnNames.left.originalToRenamed(leftColumn))
          val columnRight = rsdf(columnNames.right.originalToRenamed(rightColumn))
          (columnLeft, columnRight)
      }

    logger.debug("Prepare joining condition")
    val joinCondition = prepareJoiningCondition(renamedJoinColumnsPairs)

    logger.debug(s"$joinType Join of two DataFrames")
    val joinedDataFrame = lsdf.join(rsdf, joinCondition, joinType.toSpark)

    logger.debug("Removing additional columns in right DataFrame")
    val noDuplicatesSparkDF = sparkDFWithRemovedDuplicatedColumns(
      joinedDataFrame,
      renamedJoinColumnsPairs,
      getColumns(lsdf),
      getColumns(rsdf))

    DataFrame.fromSparkDataFrame(noDuplicatesSparkDF)
  }

  override protected def inferSchema(
      leftSchema: StructType,
      rightSchema: StructType): (StructType, InferenceWarnings) = {

    validateSchemas(leftSchema, rightSchema)
    val columnNames = RenamedColumnNames(leftSchema.fieldNames, rightSchema.fieldNames)

    val prefixedLeftSchema = StructType(
      leftSchema.map(
        field => field.copy(name = columnNames.left.originalToRenamed(field.name))))
    val prefixedRightSchema = StructType(
      rightSchema.map(
        field => field.copy(name = columnNames.right.originalToRenamed(field.name))))
    val rightJoinColumnNames = getSelectedJoinColumnsNames(
        rightSchema, _.getRightColumn)

    val columns = prefixedLeftSchema ++
      prefixedRightSchema.filter(
        col => !rightJoinColumnNames.contains(columnNames.right.renamedToOriginal(col.name)))

    (StructType(columns), InferenceWarnings.empty)
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
      JoinColumnsPairs: Seq[(Column, Column)],
      LeftColumns: Seq[Column],
      RightColumns: Seq[Column]): sql.DataFrame = {
    val (_, rightJoinColumns) = JoinColumnsPairs.unzip
    val columns = LeftColumns ++ RightColumns.filter(
      col => !rightJoinColumns.contains(col))
    assert(columns.nonEmpty)
    joinedDataFrame.select(columns: _*)
  }

  private def getSelectedJoinColumnsNames(
      schema: StructType,
      selector: ColumnPair => SingleColumnSelection): Seq[String] = {
    getJoinColumns.map(columnPair =>
      DataFrameColumnsGetter.getColumnName(schema, selector(columnPair)))
  }

  private def validateSchemas(
      leftSchema: StructType,
      rightSchema: StructType): Unit = {

    val leftJoinColumnNames = getSelectedJoinColumnsNames(
        leftSchema, _.getLeftColumn)
    val rightJoinColumnNames = getSelectedJoinColumnsNames(
        rightSchema, _.getRightColumn)

    logger.debug("Validate that columns used for joining is not empty")
    if (leftJoinColumnNames.isEmpty) {
      throw ColumnsDoNotExistException(NameColumnSelection(leftJoinColumnNames.toSet), leftSchema)
    }
    if (rightJoinColumnNames.isEmpty) {
      throw ColumnsDoNotExistException(NameColumnSelection(rightJoinColumnNames.toSet), rightSchema)
    }

    logger.debug("Validate types of columns used to join two DataFrames")
    leftJoinColumnNames.zip(rightJoinColumnNames).foreach { case (leftCol, rightCol) =>
      DataFrame.assertExpectedColumnType(
        leftSchema.apply(leftCol),
        SparkConversions.sparkColumnTypeToColumnType(rightSchema.apply(rightCol).dataType))
    }
  }

  private def prepareJoiningCondition(joinColumns: Seq[(Column, Column)]): Column = {
    require(joinColumns.nonEmpty)
    val initialCondition = joinColumns.head match {
      case (leftHead, rightHead) =>
        leftHead === rightHead
    }
    val joinCondition = joinColumns.foldLeft(initialCondition) {
      case (acc, (leftColumn, rightColumn)) =>
        acc && (leftColumn === rightColumn)
    }
    joinCondition
  }

  private def getColumns(sdf: sql.DataFrame): Seq[Column] = {
    sdf.columns.map(sdf(_))
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
      override val renamed = prefixed
    }
  }
}

object Join {

  case class ColumnPair() extends Params {

    val leftColumn = SingleColumnSelectorParam(
      name = "left column",
      description = Some("Column from the left DataFrame."),
      portIndex = 0)

    def getLeftColumn: SingleColumnSelection = $(leftColumn)
    def setLeftColumn(value: SingleColumnSelection): this.type = set(leftColumn, value)

    val rightColumn = SingleColumnSelectorParam(
      name = "right column",
      description = Some("Column from the right DataFrame."),
      portIndex = 1)

    def getRightColumn: SingleColumnSelection = $(rightColumn)
    def setRightColumn(value: SingleColumnSelection): this.type = set(rightColumn, value)

    val params: Array[ai.deepsense.deeplang.params.Param[_]] = Array(leftColumn, rightColumn)
  }

}


object JoinTypeChoice {

  sealed abstract class Option(override val name: String) extends Choice {

    val toSpark: String
    override val params: Array[Param[_]] = Array()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Inner],
      classOf[Outer],
      classOf[LeftOuter],
      classOf[RightOuter]
    )

  }

  case class Inner() extends Option("Inner") { override val toSpark = "inner"}
  case class Outer() extends Option("Outer") { override val toSpark = "outer"}
  case class LeftOuter() extends Option("Left outer") { override val toSpark = "left_outer"}
  case class RightOuter() extends Option("Right outer") { override val toSpark = "right_outer"}
}
