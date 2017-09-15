/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import java.sql.Timestamp

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{ColumnName, Row}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoricalMetadata
import io.deepsense.reportlib.model
import io.deepsense.reportlib.model._

private object StatType extends Enumeration {
  type StatType = Value
  val Categorical, Continuous = Value
}
import io.deepsense.deeplang.doperables.dataframe.StatType._

trait DataFrameReportGenerator {

  def report(sparkDataFrame: org.apache.spark.sql.DataFrame): Report = {
    val columnsSubset: List[String] =
      sparkDataFrame.columns.toList.take(DataFrameReportGenerator.maxColumnsNumberInReport)
    val limitedDataFrame = sparkDataFrame.select(columnsSubset.map(new ColumnName(_)):_*)
    val categoricalMetadata = CategoricalMetadata(sparkDataFrame)

    val (distributions, dataFrameSize) = columnsDistributions(limitedDataFrame, categoricalMetadata)
    val sampleTable = dataSampleTable(limitedDataFrame, categoricalMetadata)
    val sizeTable = dataFrameSizeTable(sparkDataFrame.schema, dataFrameSize)
    Report(ReportContent(
      "DataFrame Report",
      Map(sampleTable.name -> sampleTable, sizeTable.name -> sizeTable),
      distributions))
  }

  private def dataSampleTable(
      sparkDataFrame: org.apache.spark.sql.DataFrame,
      categoricalMetadata: CategoricalMetadata): Table = {
    val columnsNames:List[String] = sparkDataFrame.schema.fieldNames.toList
    val columnsNumber = columnsNames.size
    val rows: Array[Row] = sparkDataFrame.take(DataFrameReportGenerator.maxRowsNumberInReport)
    val values: List[List[Option[String]]] = rows.map(row =>
      (0 until columnsNumber).map(cell2String(row, _, categoricalMetadata)).toList).toList
    Table(
      DataFrameReportGenerator.dataSampleTableName,
      s"${DataFrameReportGenerator.dataSampleTableName}. " +
        s"First $columnsNumber columns and ${rows.length} randomly chosen rows",
      Some(columnsNames),
      None,
      values
    )
  }

  private def dataFrameSizeTable(schema: StructType, dataFrameSize: Long): Table =
    Table(
      DataFrameReportGenerator.dataFrameSizeTableName,
      s"${DataFrameReportGenerator.dataFrameSizeTableName}. " +
        s"Number of columns and number of rows in the DataFrame.",
      Some(List("Number of columns", "Number of rows")),
      None,
      List(List(Some(schema.fieldNames.length.toString), Some(dataFrameSize.toString)))
    )

  /**
   * Assumption that DataFrame is already projected to the interesting subset of columns
   * (up to 100 columns).
   */
  private def columnsDistributions(
      sparkDataFrame: org.apache.spark.sql.DataFrame,
      categoricalMetadata: CategoricalMetadata): (Map[String, Distribution], Long) = {
    val basicStats: MultivariateStatisticalSummary =
      Statistics.colStats(sparkDataFrame.rdd.map(row2DoubleVector(categoricalMetadata)))
    val distributions = sparkDataFrame.schema.zipWithIndex.map(p => {
      val rdd: RDD[Double] =
        columnAsDoubleRDDWithoutMissingValues(sparkDataFrame, categoricalMetadata, p._2)
      val rddSize = rdd.count()
      val missingValues: Long = basicStats.count - rddSize
      distributionType(p._1, categoricalMetadata) match {
        case Continuous => continuousDistribution(
          p._1,
          rdd,
          basicStats.min(p._2),
          basicStats.max(p._2),
          rddSize,
          missingValues,
          categoricalMetadata)
        case Categorical => categoricalDistribution(p._1, rdd, missingValues, categoricalMetadata)
      }
    })
    (distributions.map(d => d.name -> d).toMap, basicStats.count)
  }

  private def columnAsDoubleRDDWithoutMissingValues(
      sparkDataFrame: org.apache.spark.sql.DataFrame,
      categoricalMetadata: CategoricalMetadata,
      columnIndex: Int): RDD[Double] = {
    sparkDataFrame.rdd.map(cell2Double(categoricalMetadata)(_, columnIndex)).filter(!_.isNaN)
  }

  private def categoricalDistribution(
      structField: StructField,
      rdd: RDD[Double],
      missingValues: Long,
      categoricalMetadata: CategoricalMetadata): CategoricalDistribution = {
    val (labels, buckets) = bucketsForCategoricalColumn(structField, categoricalMetadata)
    val counts: Array[Long] = if (buckets.size > 1) rdd.histogram(buckets.toArray) else Array()
    CategoricalDistribution(
      structField.name,
      s"Categorical distribution for ${structField.name} column",
      missingValues,
      labels,
      counts)
  }

  private def continuousDistribution(
      structField: StructField,
      rdd: RDD[Double],
      min: Double,
      max: Double,
      rddSize: Long,
      missingValues: Long,
      categoricalMetadata: CategoricalMetadata): ContinuousDistribution = {
    val (buckets, counts) = histogram(rdd, min, max, structField, categoricalMetadata)
    val quartiles = calculateQuartiles(rdd, rddSize, structField, categoricalMetadata)
    val d2L = double2Label(categoricalMetadata)(structField)_
    val stats = model.Statistics(
      quartiles.median,
      Some(d2L(max)),
      Some(d2L(min)),
      Some(d2L(rdd.mean())),
      quartiles.first,
      quartiles.third,
      quartiles.outliers)
    ContinuousDistribution(
      structField.name,
      s"Continuous distribution for ${structField.name} column",
      missingValues,
      buckets,
      counts,
      stats)
  }

  private def bucketsForCategoricalColumn(
      structField: StructField,
      categoricalMetadata: CategoricalMetadata): (IndexedSeq[String], IndexedSeq[Double]) =
    structField.dataType match {
      case BooleanType =>
        (IndexedSeq(false.toString, true.toString), IndexedSeq(0.0, 1.0, 1.1))
      case IntegerType if categoricalMetadata.isCategorical(structField.name) =>
        val mapping = categoricalMetadata.mapping(structField.name)
        val sortedIds: IndexedSeq[Int] = mapping.ids.sortWith(_ < _).toIndexedSeq
        (sortedIds.map(mapping.idToValue),
          sortedIds.map(_.toDouble) ++ IndexedSeq(sortedIds.last.toDouble + 0.1))
      case StringType => (IndexedSeq(), IndexedSeq())
    }

  private def histogram(
      rdd: RDD[Double],
      min: Double,
      max: Double,
      structField: StructField,
      categoricalMetadata: CategoricalMetadata): (Seq[String], Seq[Long]) = {
    val steps: Int = numberOfSteps(min, max, structField.dataType)
    val buckets: Array[Double] = customRange(min, max, steps)
    (buckets2Labels(buckets.toList.take(buckets.length - 1), structField, categoricalMetadata),
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
      rdd: RDD[Double],
      size: Long,
      structField: StructField,
      categoricalMetadata: CategoricalMetadata): Quartiles =
    if (size > 0) {
      val sortedRdd = rdd.sortBy(identity).zipWithIndex().map {
        case (v, idx) => (idx, v)
      }
      val d2L = double2Label(categoricalMetadata)(structField)_
      val secondQuartile: Option[String] = Some(d2L(median(sortedRdd, 0, size)))
      if (size >= 3) {
        val firstQuartile: Double = median(sortedRdd, 0, size / 2)
        val thirdQuartile: Double = median(sortedRdd, size / 2 + size % 2, size)
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

  private def row2DoubleVector(categoricalMetadata: CategoricalMetadata)(row: Row): Vector = {
    Vectors.dense((0 until row.size).map(cell2Double(categoricalMetadata)(row, _)).toArray)
  }

  private def cell2Double(categoricalMetadata: CategoricalMetadata)(row: Row, index: Int): Double =
    if (row.isNullAt(index)) {
      Double.NaN
    } else {
      row.schema(index).dataType match {
        case LongType => row.getLong(index).toDouble
        case TimestampType => row.getAs[Timestamp](index).getTime.toDouble
        case StringType => 0L
        case BooleanType => categorical2Double(
          row.getBoolean(index).toString,
          List(false.toString, true.toString))
        case DoubleType => row.getDouble(index)
        case IntegerType if categoricalMetadata.isCategorical(index) => row.getInt(index).toDouble
      }
    }

  private def buckets2Labels(
      buckets: Seq[Double],
      structField: StructField,
      categoricalMetadata: CategoricalMetadata): Seq[String] =
    buckets.map(double2Label(categoricalMetadata)(structField))

  private def double2Label(
      categoricalMetadata: CategoricalMetadata)(
      structField: StructField)(
      d: Double): String = structField.dataType match {
    case BooleanType => if (d == 0D) false.toString else true.toString
    case LongType => DoubleUtils.double2String(d)
    case DoubleType => DoubleUtils.double2String(d)
    case TimestampType =>
      DateTimeConverter.toString(DateTimeConverter.fromMillis(d.toLong))
    case IntegerType if categoricalMetadata.isCategorical(structField.name) =>
      categoricalMetadata.mapping(structField.name).idToValue(d.toInt)
  }

  private def distributionType(
      structField: StructField,
      categoricalMetadata: CategoricalMetadata): StatType = structField.dataType match {
    case LongType => Continuous
    case TimestampType => Continuous
    case DoubleType => Continuous
    case StringType => Categorical
    case BooleanType => Categorical
    case IntegerType if categoricalMetadata.isCategorical(structField.name)  => Categorical
  }

  private def categorical2Double(value: String, possibleValues: List[String]): Double =
    possibleValues.indexOf(value).toDouble

  private def cell2String(
      row: Row,
      index: Int,
      categoricalMetadata: CategoricalMetadata): Option[String] = {
    val structField: StructField = row.schema.apply(index)
    if (row.isNullAt(index)) {
      None
    } else {
      structField.dataType match {
        case TimestampType => Some(DateTimeConverter.toString(
          DateTimeConverter.fromMillis(row.get(index).asInstanceOf[Timestamp].getTime)))
        case DoubleType => Some(DoubleUtils.double2String(row.getDouble(index)))
        case IntegerType if categoricalMetadata.isCategorical(index) =>
          Some(categoricalMetadata.mapping(index).idToValue(row.getInt(index)))
        case _ => Some(row(index).toString)
      }
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

