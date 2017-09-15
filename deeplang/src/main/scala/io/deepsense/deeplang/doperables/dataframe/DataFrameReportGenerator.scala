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

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql
import org.apache.spark.sql.types._
import org.apache.spark.sql.{ColumnName, Row}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.ReportLevel.ReportLevel
import io.deepsense.deeplang.doperables.dataframe.types.SparkConversions
import io.deepsense.deeplang.doperables.{Report, ReportLevel}
import io.deepsense.reportlib.model
import io.deepsense.reportlib.model._

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
    val columnsSubset: List[String] =
      sparkDataFrame.columns.toList.take(DataFrameReportGenerator.maxColumnsNumberInReport)
    val limitedDataFrame = sparkDataFrame.select(columnsSubset.map(new ColumnName(_)): _*)

    val (distributions, dataFrameSize) =
      columnsDistributions(executionContext, limitedDataFrame, dataFrameEmpty)
    val sampleTable = dataSampleTable(limitedDataFrame)
    val sizeTable = dataFrameSizeTable(sparkDataFrame.schema, dataFrameSize)
    Report(ReportContent(
      "DataFrame Report",
      Map(sampleTable.name -> sampleTable, sizeTable.name -> sizeTable),
      distributions))
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
        s"First $columnsNumber columns and ${rows.length} randomly chosen rows",
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

  /**
   * Assumption that DataFrame is already filtered to the interesting subset of columns
   * (up to 100 columns).
   */
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

    if (executionContext.reportLevel == ReportLevel.LOW) {
      // Turn off generating any distributions when reportLevel == LOW
      (Map(), dataFrameSize)
    } else {
      val distributions = sparkDataFrame.schema.zipWithIndex.flatMap {
        case (structField, index) => {
          val rdd: RDD[Double] =
            columnAsDoubleRDDWithoutMissingValues(sparkDataFrame, index)
          rdd.cache()
          distributionType(structField) match {
            case Continuous =>
              val basicStatsForColumnOption = if (rdd.isEmpty()) { None } else { basicStats }
              Some(continuousDistribution(
                structField,
                rdd,
                basicStatsForColumnOption.map(_.min(index)),
                basicStatsForColumnOption.map(_.max(index)),
                dataFrameSize,
                executionContext.reportLevel))
            case Discrete =>
              Some(discreteDistribution(dataFrameSize, structField, rdd))
            case Empty => None
          }
        }
      }
      (distributions.map(d => d.name -> d).toMap, dataFrameSize)
    }
  }

  private def columnAsDoubleRDDWithoutMissingValues(
      sparkDataFrame: org.apache.spark.sql.DataFrame,
      columnIndex: Int): RDD[Double] =
    sparkDataFrame.rdd.map(cell2Double(_, columnIndex)).filter(!_.isNaN)

  private def discreteDistribution(
      dataFrameSize: Long,
      structField: StructField,
      rdd: RDD[Double]): DiscreteDistribution = {
    val (labels, buckets) = bucketsForDiscreteColumn(structField)
    val counts: Array[Long] = if (buckets.size > 1) rdd.histogram(buckets.toArray) else Array()
    val rddSize: Long = if (counts.nonEmpty) counts.fold(0L)(_ + _) else rdd.count()
    DiscreteDistribution(
      structField.name,
      s"Discrete distribution for ${structField.name} column",
      dataFrameSize - rddSize,
      labels,
      counts)
  }

  private def bucketsForDiscreteColumn(
      structField: StructField): (IndexedSeq[String], IndexedSeq[Double]) =

    structField.dataType match {
      case BooleanType =>
        (IndexedSeq(false.toString, true.toString), IndexedSeq(0.0, 1.0, 1.1))
      case StringType => (IndexedSeq(), IndexedSeq())
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
    val quartiles = calculateQuartiles(rddSize, rdd, structField, reportLevel)
    val d2L = double2Label(structField)_
    val mean = if (min.isEmpty || max.isEmpty) None else Some(rdd.mean())
    val stats = model.Statistics(
      quartiles.median,
      max.map(d2L),
      min.map(d2L),
      mean.map(d2L),
      quartiles.first,
      quartiles.third,
      quartiles.outliers)
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

  private def numberOfSteps(min: Double, max: Double, dataType: DataType): Int =
    if (max - min < DataFrameReportGenerator.doubleTolerance) {
      1
    } else if (dataType == LongType || dataType == TimestampType) {
      Math.min(max.toLong - min.toLong + 1, DataFrameReportGenerator.defaultBucketsNumber).toInt
    } else {
      DataFrameReportGenerator.defaultBucketsNumber
    }

  private def calculateQuartiles(
      rddSize: Long,
      rdd: RDD[Double],
      structField: StructField,
      reportLevel: ReportLevel): Quartiles =
    if (rddSize > 0 && reportLevel == ReportLevel.HIGH) {
      val sortedRdd = rdd.sortBy(identity).zipWithIndex().map {
        case (v, idx) => (idx, v)
      }
      sortedRdd.cache()
      val d2L = double2Label(structField) _
      val secondQuartile: Option[String] = Some(d2L(median(sortedRdd, 0, rddSize)))
      if (rddSize >= 3) {
        val firstQuartile: Double = median(sortedRdd, 0, rddSize / 2)
        val thirdQuartile: Double = median(sortedRdd, rddSize / 2 + rddSize % 2, rddSize)
        val outliers: Array[Double] = findOutliers(rdd, firstQuartile, thirdQuartile)
        Quartiles(
          Some(d2L(firstQuartile)),
          secondQuartile,
          Some(d2L(thirdQuartile)),
          outliers.map(d2L))
      } else {
        Quartiles(None, secondQuartile, None, Seq())
      }
    } else {
      Quartiles(None, None, None, Seq())
    }

  private def findOutliers(
      rdd: RDD[Double],
      firstQuartile: Double,
      thirdQuartile: Double): Array[Double] = {
    val k = (thirdQuartile - firstQuartile) * DataFrameReportGenerator.outlierConstant
    val lowerBound = firstQuartile - k
    val upperBound = thirdQuartile + k
    rdd.filter(d => d < lowerBound || d > upperBound).collect()
  }

  private def median(sortedRdd: RDD[(Long, Double)], start: Long, end: Long): Double = {
    val s = start + end
    if (s % 2 == 0) {
      val r = s / 2
      val l = r - 1
      (sortedRdd.lookup(l).head + sortedRdd.lookup(r).head) / 2
    } else {
      sortedRdd.lookup(s / 2).head
    }
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
        case LongType => row.getLong(index).toDouble
        case TimestampType => row.getAs[Timestamp](index).getTime.toDouble
        case StringType => 0L
        case BooleanType => discrete2Double(
          row.getBoolean(index).toString,
          List(false.toString, true.toString))
        case DoubleType => row.getDouble(index)
        case IntegerType => row.getInt(index).toDouble
      }
    }

  private def buckets2Labels(
      buckets: Seq[Double],
      structField: StructField): Seq[String] =
    buckets.map(double2Label(structField))

  private def double2Label(
      structField: StructField)(
      d: Double): String = structField.dataType match {
    case BooleanType => if (d == 0D) false.toString else true.toString
    case LongType => DoubleUtils.double2String(d)
    case DoubleType => DoubleUtils.double2String(d)
    case TimestampType =>
      DateTimeConverter.toString(DateTimeConverter.fromMillis(d.toLong))
    case IntegerType => d.toInt.toString
  }

  private def distributionType(
      structField: StructField): StatType = structField.dataType match {
    case LongType => Continuous
    case TimestampType => Continuous
    case DoubleType => Continuous
    case StringType => Empty
    case BooleanType => Discrete
    case IntegerType => Continuous
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
        case TimestampType => Some(DateTimeConverter.toString(
          DateTimeConverter.fromMillis(row.get(index).asInstanceOf[Timestamp].getTime)))
        case DoubleType => Some(DoubleUtils.double2String(row.getDouble(index)))
        case _ => Some(row(index).toString)
      }
    }
  }

  private def safeDataFrameCache(sparkDataFrame: sql.DataFrame): Unit = {
    if (!sparkDataFrame.schema.isEmpty) {
      sparkDataFrame.cache()
    }
  }
}

object DataFrameReportGenerator extends DataFrameReportGenerator {
  val defaultBucketsNumber = 20
  val dataSampleTableName = "Data Sample"
  val dataFrameSizeTableName = "DataFrame Size"
  val maxRowsNumberInReport = 10
  val maxColumnsNumberInReport = 10
  val outlierConstant = 1.5
  val doubleTolerance = 0.000001
}

private case class Quartiles(
  first: Option[String],
  median: Option[String],
  third: Option[String],
  outliers: Seq[String])
