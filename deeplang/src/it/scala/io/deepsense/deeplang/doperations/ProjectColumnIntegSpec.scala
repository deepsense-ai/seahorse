/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import java.sql.Timestamp

import scala.collection.JavaConverters._

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Ignore, Matchers}

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.ColumnsDoesNotExistException
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.parameters._

@Ignore
class ProjectColumnIntegSpec
  extends DeeplangIntegTestSupport
  with GeneratorDrivenPropertyChecks
  with Matchers {

  val columns = Seq(
    StructField("c", DoubleType),
    StructField("b", StringType),
    StructField("a", DoubleType),
    StructField("x", TimestampType),
    StructField("z", BooleanType))

  def schema = StructType(columns)

  // Projected:     "b"     "a"                                               "z"
  val row1 = Seq(1, "str1", 10.0, new Timestamp(DateTime.now.getMillis), true)
  val row2 = Seq(2, "str2", 20.0, new Timestamp(DateTime.now.getMillis), false)
  val row3 = Seq(3, "str3", 30.0, new Timestamp(DateTime.now.getMillis), false)
  val data = Seq(row1, row2, row3)

  "ProjectColumn" should "select correct columns basing on the column selection" in {
    val projected = projectColumns(Set("z", "b"), Set(1, 2), Set(ColumnType.ordinal))
    val selectedIndices = Set(1, 2, 5)
    val expectedColumns = selectWithIndices[StructField](selectedIndices, columns)
    val expectedSchema = StructType(expectedColumns)
    val expectedData = data.map(r => selectWithIndices[Any](selectedIndices, r.toList))
    val expectedDataFrame = createDataFrame(expectedData.map(Row.fromSeq), expectedSchema)
    assertDataFramesEqual(projected, expectedDataFrame)
  }

  "ProjectColumn" should "throw an exception when the columns selected by name does not exist" in {
    intercept[ColumnsDoesNotExistException]{
      val nonExistingColumnName = "thisColumnDoesNotExist"
      projectColumns(
        Set(nonExistingColumnName),
        Set.empty,
        Set.empty)
    }
  }

  it should "throw an exception when the columns selected by index does not exist" in {
    intercept[ColumnsDoesNotExistException]{
      val nonExistingColumnIndex = 1000
      projectColumns(
        Set.empty,
        Set(nonExistingColumnIndex),
        Set.empty)
    }
  }

  it should "produce an empty set when selecting a type that does not exist" in {
    val emptyDataFrame = projectColumns(
      Set.empty,
      Set.empty,
      Set(ColumnType.ordinal))
    emptyDataFrame.sparkDataFrame.collectAsList().asScala shouldBe empty
  }

  it should "produce an empty set on empty selection" in {
    val emptyDataFrame = projectColumns(
      Set.empty,
      Set.empty,
      Set.empty)
    emptyDataFrame.sparkDataFrame.collectAsList().asScala shouldBe empty
  }

  private def projectColumns(
      names: Set[String],
      ids: Set[Int],
      types: Set[ColumnType]): DataFrame = {
    val rdd = sparkContext.parallelize(data.map(Row.fromSeq))
    val testDataFrame = executionContext.dataFrameBuilder.buildDataFrame(schema, rdd)
    executeOperation(
      executionContext,
      operation(names, ids, types))(testDataFrame)
  }

  private def operation(
      names: Set[String],
      ids: Set[Int],
      types: Set[ColumnType]): ProjectColumns = {
    val operation = new ProjectColumns
    val valueParam = operation.parameters.getColumnSelectorParameter(operation.selectedColumns)
    valueParam.value = Some(MultipleColumnSelection(Vector(
      NameColumnSelection(names),
      IndexColumnSelection(ids),
      TypeColumnSelection(types))))
    operation
  }

  private def executeOperation(context: ExecutionContext, operation: DOperation)
      (dataFrame: DataFrame): DataFrame = {
    val operationResult = operation.execute(context)(Vector[DOperable](dataFrame))
    operationResult.head.asInstanceOf[DataFrame]
  }

  private def selectWithIndices[T](indices: Set[Int], sequence: Seq[T]): Seq[T] =
    sequence.zipWithIndex.filter { case (_, index) =>
      indices.contains(index)
    }.map { case (value, _) => value }
}
