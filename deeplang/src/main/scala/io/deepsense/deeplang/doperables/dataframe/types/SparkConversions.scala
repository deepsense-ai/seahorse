/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe.types

import org.apache.spark.sql

import io.deepsense.deeplang.parameters.ColumnType
import io.deepsense.deeplang.parameters.ColumnType._

object SparkConversions {

  private val sparkColumnTypeToColumnTypeMap: Map[ColumnType, sql.types.DataType] = Map(
    ColumnType.numeric -> sql.types.DoubleType,
    ColumnType.string -> sql.types.StringType,
    ColumnType.boolean -> sql.types.BooleanType,
    ColumnType.timestamp -> sql.types.TimestampType,
    ColumnType.ordinal -> sql.types.LongType,
    ColumnType.categorical -> sql.types.IntegerType
  )

  private val columnTypeToSparkColumnTypeMap: Map[sql.types.DataType, ColumnType] =
    sparkColumnTypeToColumnTypeMap.map(_.swap).toMap

  def columnTypeToSparkColumnType(columnType: ColumnType): sql.types.DataType =
    sparkColumnTypeToColumnTypeMap(columnType)

  def sparkColumnTypeToColumnType(sparkColumnType: sql.types.DataType): ColumnType =
    columnTypeToSparkColumnTypeMap(sparkColumnType)
}
