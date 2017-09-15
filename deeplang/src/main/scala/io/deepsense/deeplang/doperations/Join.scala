/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import scala.collection.mutable

import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.sql
import org.apache.spark.sql.{Row, Column}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.{DataFrameBuilder, DataFrame}
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoricalMetadata, CategoricalMapper}
import io.deepsense.deeplang.doperations.exceptions.ColumnsDoNotExistException
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation2To1, ExecutionContext}

class Join extends DOperation2To1[DataFrame, DataFrame, DataFrame] with LazyLogging {

  override val name = "Join"

  override val id: Id = "06374446-3138-4cf7-9682-f884990f3a60"

  override val parameters: ParametersSchema = ParametersSchema(
    Join.joinColumnsParamKey -> ColumnSelectorParameter(
      "Columns to be LEFT JOINed upon",
      required = true)
  )

  override protected def _execute(context: ExecutionContext)
                                 (ldf: DataFrame, rdf: DataFrame): DataFrame = {
    logger.info("Execution of " + this.getClass.getSimpleName + " starts")
    val joinColumns = parameters.getColumnSelectorParameter(Join.joinColumnsParamKey).value.get

    val leftJoinColumnNames = ldf.getColumnNames(joinColumns)
    val rightJoinColumnNames = rdf.getColumnNames(joinColumns)

    val lsdf = ldf.sparkDataFrame
    var rsdf = rdf.sparkDataFrame

    logger.debug("Validate that columns used for joining is not empty")
    if (leftJoinColumnNames.isEmpty) {
      throw ColumnsDoNotExistException(joinColumns.selections, ldf)
    }

    logger.debug("Validate names of columns used to join two DataFrames")
    if (leftJoinColumnNames.toSet != rightJoinColumnNames.toSet) {
      throw ColumnsDoNotExistException(
        joinColumns.selections,
        if (leftJoinColumnNames.size < rightJoinColumnNames.size) ldf else rdf)
    }

    logger.debug("Validate types of columns used to join two DataFrames")
    leftJoinColumnNames.foreach { col =>
      DataFrame.assertExpectedColumnType(
        lsdf.schema.apply(col),
        DataFrame.sparkColumnTypeToColumnType(rsdf.schema.apply(col).dataType))
    }

    logger.debug("Prepare rename columns map")
    val renamedJoinColumns = new mutable.HashMap[String, String]()
    rightJoinColumnNames.foreach { col =>
      val newCol = rdf.uniqueColumnName(col, "join")
      renamedJoinColumns.put(col, newCol)
      rsdf = rsdf.withColumnRenamed(col, newCol)
    }

    logger.debug("Rename columns in right DataFrame if they are also present in left DataFrame")
    val rightColumnsSet = rsdf.columns.toSet
    lsdf.columns.foreach { col =>
      if (rightColumnsSet.contains(col)) {
        val newCol = rdf.uniqueColumnName(col, "join")
        rsdf = rsdf.withColumnRenamed(col, newCol)
      }
    }

    logger.debug("Change CategoricalMapping in right DataFrame to allow join with left DataFrame")
    val lcm = CategoricalMetadata(lsdf)
    val rcm = CategoricalMetadata(rsdf)
    leftJoinColumnNames.foreach { col =>
      if (lcm.isCategorical(col)) {
        val lMapping = lcm.mapping(col)
        val rMapping = rcm.mapping(renamedJoinColumns(col))

        val merged = lMapping.mergeWith(rMapping)
        val otherToFinal = merged.otherToFinal

        val columnIndex = rsdf.columns.indexOf(renamedJoinColumns(col))
        val rddWithFinalMapping = rsdf.map { r =>
          val id = r.getInt(columnIndex)
          val mappedId = otherToFinal.mapId(id)
          Row.fromSeq(r.toSeq.updated(columnIndex, mappedId))
        }
        rsdf = context.sqlContext.createDataFrame(rddWithFinalMapping, rsdf.schema)
      }
    }

    logger.debug("Prepare joining condition")
    @unchecked
    val joinCondition =
      leftJoinColumnNames.toList match {
        case head :: tail =>
          tail.foldLeft(columnEqualityCondition(head, lsdf, renamedJoinColumns(head), rsdf)) {
            (acc, col) => acc && columnEqualityCondition(col, lsdf, renamedJoinColumns(col), rsdf)
          }
      }

    logger.debug("Joining two DataFrames")
    val joinedDataFrame = ldf.sparkDataFrame.join(rsdf, joinCondition, "left_outer")

    logger.debug("Removing additional columns in right DataFrame")
    val columns = ldf.sparkDataFrame.columns ++
      rsdf.columns.filter(col => !renamedJoinColumns.valuesIterator.contains(col))
    assert(columns.nonEmpty)
    val duplicateColumnsRemoved = joinedDataFrame.select(columns.head, columns.tail:_*)

    // NOTE: We perform only LEFT OUTER JOIN, thus we do not need to change CategoricalMetadata
    // in resultDataFrame (CategoricalMetadata for left DataFrame, already there is sufficient)
    val resultDataFrame = context.dataFrameBuilder.buildDataFrame(duplicateColumnsRemoved)
    logger.info("Execution of " + this.getClass.getSimpleName + " ends")
    resultDataFrame
  }

  private def columnEqualityCondition(
      leftColumnName: String,
      leftSparkDataFrame: sql.DataFrame,
      rightColumnName: String,
      rightSparkDataFrame: sql.DataFrame): Column = {
    leftSparkDataFrame(leftColumnName) === rightSparkDataFrame(rightColumnName)
  }
}

object Join {
  val joinColumnsParamKey = "joinColumns"
}
