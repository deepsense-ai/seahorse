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

package io.deepsense.deeplang.doperables.dataframe

import java.sql.Timestamp

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.ReportLevel.ReportLevel
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.{Report, ReportLevel}
import io.deepsense.deeplang.utils.SparkUtils
import io.deepsense.reportlib.model
import io.deepsense.reportlib.model._
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.types._
import org.apache.spark.sql.{ColumnName, Row}
import org.joda.time.{LocalDate, DateTime}

private object StatType extends Enumeration {
  type StatType = Value
  val Discrete, Continuous, Empty = Value
}
import io.deepsense.deeplang.doperables.dataframe.StatType._

trait DataFrameReportGenerator {

  def report(
      executionContext: ExecutionContext,
      sparkDataFrame: org.apache.spark.sql.DataFrame): Report = {

    val dataFrameEmpty: Boolean = sparkDataFrame.rdd.isEmpty()
    safeDataFrameCache(sparkDataFrame)

    val (distributions, dataFrameSize) =
      columnsDistributions(executionContext, sparkDataFrame, dataFrameEmpty)
    val sampleTable = dataSampleTable(sparkDataFrame)
    val sizeTable = dataFrameSizeTable(sparkDataFrame.schema, dataFrameSize)
    Report(ReportContent(
      "DataFrame Report",
      Map(sampleTable.name -> sampleTable, sizeTable.name -> sizeTable),
      distributions,
      Some(sparkDataFrame.schema)))
  }

  private def dataSampleTable(
      sparkDataFrame: org.apache.spark.sql.DataFrame): Table = {
    val columnsNames: List[String] = sparkDataFrame.schema.fieldNames.toList
    val columnsNumber = columnsNames.size
    val rows: Array[Row] = sparkDataFrame.take(DataFrameReportGenerator.maxRowsNumberInReport)
    val values: List[List[Option[String]]] = rows.map(row =>
      (0 until columnsNumber).map(cell2String(row, _)).toList).toList
    val columnTypes: List[ColumnType.ColumnType] = sparkDataFrame.schema.map(
      field => SparkConversions.sparkColumnTypeToColumnType(field.dataType)
    ).toList
    Table(
      DataFrameReportGenerator.dataSampleTableName,
      s"${DataFrameReportGenerator.dataSampleTableName}. " +
        s"First ${rows.length} randomly chosen rows",
      Some(columnsNames),
      columnTypes,
      None,
      values)
  }

  private def dataFrameSizeTable(schema: StructType, dataFrameSize: Long): Table =
    Table(
      DataFrameReportGenerator.dataFrameSizeTableName,
      s"${DataFrameReportGenerator.dataFrameSizeTableName}. " +
        s"Number of columns and number of rows in the DataFrame.",
      Some(List("Number of columns", "Number of rows")),
      List(ColumnType.numeric, ColumnType.numeric),
      None,
      List(List(Some(schema.fieldNames.length.toString), Some(dataFrameSize.toString))))

  private def columnsDistributions(
      executionContext: ExecutionContext,
      sparkDataFrame: org.apache.spark.sql.DataFrame,
      dataFrameEmpty: Boolean): (Map[String, Distribution], Long) = {

    safeDataFrameCache(sparkDataFrame)
    val basicStats: Option[MultivariateStatisticalSummary] =
      if (dataFrameEmpty) {
        None
      } else {
        Some(Statistics.colStats(sparkDataFrame.rdd.map(row2DoubleVector)))
      }
    val dataFrameSize: Long = basicStats.map(_.count).getOrElse(0L)

    val distributions = sparkDataFrame.schema.zipWithIndex.flatMap {
      case (structField, index) => {
        distributionType(structField) match {
          case Continuous =>
            val rdd: RDD[Double] =
              columnAsDoubleRDDWithoutMissingValues(sparkDataFrame, index)
            rdd.cache()
            val basicStatsForColumnOption = if (rdd.isEmpty()) { None } else { basicStats }
            Some(continuousDistribution(
              structField,
              rdd,
              basicStatsForColumnOption.map(_.min(index)),
              basicStatsForColumnOption.map(_.max(index)),
              dataFrameSize,
              executionContext.reportLevel))
          case Discrete =>
            val rdd = columnAsStringRDDWithoutMissingValues(sparkDataFrame, index)
            Some(discreteDistribution(dataFrameSize, structField, rdd))
          case Empty => None
        }
      }
    }
    (distributions.map(d => d.name -> d).toMap, dataFrameSize)
  }

  private def columnAsDoubleRDDWithoutMissingValues(
      sparkDataFrame: org.apache.spark.sql.DataFrame,
      columnIndex: Int): RDD[Double] =
    sparkDataFrame.rdd.map(cell2Double(_, columnIndex)).filter(!_.isNaN)

  def columnAsStringRDDWithoutMissingValues(
      sparkDataFrame: org.apache.spark.sql.DataFrame,
      columnIndex: Int): RDD[String] =
    sparkDataFrame.rdd.flatMap(cell2String(_, columnIndex))

  private def discreteDistribution(
      dataFrameSize: Long,
      structField: StructField,
      rdd: RDD[String]): DiscreteDistribution = {
    val (labels, counts) = getLabelsAndCounts(structField, rdd)
    val rddSize: Long = if (counts.nonEmpty) counts.fold(0L)(_ + _) else rdd.count()
    DiscreteDistribution(
      structField.name,
      s"Discrete distribution for ${structField.name} column",
      dataFrameSize - rddSize,
      labels,
      counts)
  }

  private def getLabelsAndCounts(
      structField: StructField,
      rdd: RDD[String]): (Seq[String], Seq[Long]) = {
    val maybeOccurencesMap = SparkUtils.countOccurrencesWithKeyLimit(
      rdd, DataFrameReportGenerator.maxDistinctValuesToCalculateDistribution)
    maybeOccurencesMap.map(occurencesMap => {
      val labels = structField.dataType match {
        case StringType => occurencesMap.keys.toSeq.sorted
        case BooleanType => Seq("false", "true")
      }
      val counts = labels.map(occurencesMap.getOrElse(_, 0L))
      (labels, counts)
    }).getOrElse((Seq.empty, Seq.empty))
  }

  private def continuousDistribution(
      structField: StructField,
      rdd: RDD[Double],
      min: Option[Double],
      max: Option[Double],
      dataFrameSize: Long,
      reportLevel: ReportLevel): ContinuousDistribution = {
    val (buckets, counts) =
      if (min.isEmpty || max.isEmpty) {
        (Seq(), Seq())
      } else {
        histogram(rdd, min.get, max.get, structField)
      }
    val rddSize: Long = counts.fold(0L)(_ + _)
    val d2L = double2Label(structField)_
    val mean2L = mean2Label(structField)_
    val mean = if (min.isEmpty || max.isEmpty) None else Some(rdd.mean())
    val stats = model.Statistics(
      max.map(d2L),
      min.map(d2L),
      mean.map(mean2L))
    ContinuousDistribution(
      structField.name,
      s"Continuous distribution for ${structField.name} column",
      dataFrameSize - rddSize,
      buckets,
      counts,
      stats)
  }

  private def histogram(
      rdd: RDD[Double],
      min: Double,
      max: Double,
      structField: StructField): (Seq[String], Seq[Long]) = {
    val steps: Int = numberOfSteps(min, max, structField.dataType)
    val buckets: Array[Double] = customRange(min, max, steps)
    (buckets2Labels(buckets.toList, structField),
      rdd.histogram(buckets))
  }

  def isIntegerLike(dataType: DataType): Boolean =
    dataType match {
      case ByteType | ShortType | IntegerType | LongType | TimestampType | DateType => true
      case _ => false
    }

  private def numberOfSteps(min: Double, max: Double, dataType: DataType): Int =
    if (max - min < DataFrameReportGenerator.doubleTolerance) {
      1
    } else if (isIntegerLike(dataType)) {
      Math.min(max.toLong - min.toLong + 1, DataFrameReportGenerator.defaultBucketsNumber).toInt
    } else {
      DataFrameReportGenerator.defaultBucketsNumber
    }

  private def customRange(min: Double, max: Double, steps: Int): Array[Double] = {
    val span = max - min
    (Range.Int(0, steps, 1).map(s => min + (s * span) / steps) :+ max).toArray
  }

  private def row2DoubleVector(row: Row): Vector =
    Vectors.dense((0 until row.size).map(cell2Double(row, _)).toArray)

  private def cell2Double(row: Row, index: Int): Double =
    if (row.isNullAt(index)) {
      Double.NaN
    } else {
      row.schema(index).dataType match {
        case ByteType => row.getByte(index).toDouble
        case ShortType => row.getShort(index).toDouble
        case IntegerType => row.getInt(index).toDouble
        case LongType => row.getLong(index).toDouble
        case FloatType => row.getFloat(index).toDouble
        case DoubleType => row.getDouble(index)
        case _: DecimalType => row.getDecimal(index).doubleValue()
        case BooleanType => discrete2Double(
          row.getBoolean(index).toString,
          List(false.toString, true.toString))
        case TimestampType => row.getAs[Timestamp](index).getTime.toDouble
        case DateType => dateToDouble(row.getDate(index))
        // We return 0 to call Statistics.colStats on the whole DataFrame
        case _ => 0L
      }
    }

  private def dateToDouble(date: java.sql.Date): Double = {
    val localDate = new LocalDate(date)
    new DateTime(
      localDate.getYear,
      localDate.getMonthOfYear,
      localDate.getDayOfMonth,
      0,
      0,
      DateTimeConverter.zone).getMillis
  }

  private def buckets2Labels(
      buckets: Seq[Double],
      structField: StructField): Seq[String] =
    buckets.map(double2Label(structField))

  private def double2Label(
      structField: StructField)(
      d: Double): String = structField.dataType match {
    case ByteType => d.toByte.toString
    case ShortType => d.toShort.toString
    case IntegerType => d.toInt.toString
    case LongType => d.toLong.toString
    case FloatType | DoubleType | _: DecimalType => DoubleUtils.double2String(d)
    case BooleanType => if (d == 0D) false.toString else true.toString
    case TimestampType | DateType =>
      DateTimeConverter.toString(DateTimeConverter.fromMillis(d.toLong))
  }

  /**
    * We want to present mean of integer-like values as a floating point number, however
    * dates, timestamps and booleans should be converted to their original type.
    */
  private def mean2Label(structField: StructField)(d: Double): String = structField.dataType match {
    case ByteType | ShortType | IntegerType | LongType => DoubleUtils.double2String(d)
    case _ => double2Label(structField)(d)
  }

  private def distributionType(
      structField: StructField): StatType = structField.dataType match {
    case ByteType => Continuous
    case ShortType => Continuous
    case IntegerType => Continuous
    case LongType => Continuous
    case FloatType => Continuous
    case DoubleType => Continuous
    case _: DecimalType => Continuous
    case StringType => Discrete
    case BinaryType => Empty
    case BooleanType => Discrete
    case TimestampType => Continuous
    case DateType => Continuous
    case _: ArrayType => Empty
    case _: MapType => Empty
    case _: StructType => Empty
    case _: VectorUDT => Empty
  }

  private def discrete2Double(value: String, possibleValues: List[String]): Double =
    possibleValues.indexOf(value).toDouble

  private def cell2String(
      row: Row,
      index: Int): Option[String] = {
    val structField: StructField = row.schema.apply(index)
    if (row.isNullAt(index)) {
      None
    } else {
      structField.dataType match {
        case FloatType => Some(DoubleUtils.double2String(row.getFloat(index)))
        case DoubleType => Some(DoubleUtils.double2String(row.getDouble(index)))
        case _: DecimalType => Some(row.getDecimal(index).toPlainString)
        case TimestampType => Some(DateTimeConverter.toString(
          DateTimeConverter.fromMillis(row.get(index).asInstanceOf[Timestamp].getTime)))
        case DateType => Some(DateTimeConverter.toString(
          DateTimeConverter.fromMillis(row.getDate(index).getTime)))
        case _ => Some(row(index).toString)
      }
    }
  }

  private def safeDataFrameCache(sparkDataFrame: sql.DataFrame): Unit = {
    if (sparkDataFrame.schema.nonEmpty) {
      sparkDataFrame.cache()
    }
  }
}

object DataFrameReportGenerator extends DataFrameReportGenerator {
  val defaultBucketsNumber = 20
  val dataSampleTableName = "Data Sample"
  val dataFrameSizeTableName = "DataFrame Size"
  val maxRowsNumberInReport = 10
  val doubleTolerance = 0.000001
  val maxDistinctValuesToCalculateDistribution = 10
}
