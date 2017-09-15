/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import java.math.RoundingMode
import java.sql.Timestamp
import java.text.{DecimalFormat, DecimalFormatSymbols}
import java.util.Locale

import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{ColumnName, Row}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.doperables.Report
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

    val (distributions, dataFrameSize) = columnsDistributions(limitedDataFrame)
    val sampleTable = dataSampleTable(limitedDataFrame)
    val sizeTable = dataFrameSizeTable(sparkDataFrame.schema, dataFrameSize)
    Report(ReportContent(
      "DataFrame Report",
      Map(sampleTable.name -> sampleTable, sizeTable.name -> sizeTable),
      distributions))
  }

  private def dataSampleTable(sparkDataFrame: org.apache.spark.sql.DataFrame): Table = {
    val columnsNames:List[String] = sparkDataFrame.schema.fieldNames.toList
    val columnsNumber = columnsNames.size
    val rows: Array[Row] = sparkDataFrame.take(DataFrameReportGenerator.maxRowsNumberInReport)
    val values: List[List[String]] = rows.map(row =>
      (0 until columnsNumber).map(cell2String(row, _)).toList).toList
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
      List(List(schema.fieldNames.length.toString, dataFrameSize.toString))
    )

  /**
   * Assumption that DataFrame is already projected to the interesting subset of columns
   * (up to 100 columns).
   */
  private def columnsDistributions(
      sparkDataFrame: org.apache.spark.sql.DataFrame): (Map[String, Distribution], Long) = {
    val basicStats: MultivariateStatisticalSummary =
      Statistics.colStats(sparkDataFrame.rdd.map(row2DoubleVector))
    val distributions = sparkDataFrame.schema.zipWithIndex.map(p => {
      val rdd: RDD[Double] = columnAsDoubleRDDWithoutMissingValues(sparkDataFrame, p._2)
      val rddSize = rdd.count()
      val missingValues: Long = basicStats.count - rddSize
      distributionType(p._1.dataType) match {
        case Continuous => continuousDistribution(
          p._1,
          rdd,
          basicStats.min(p._2),
          basicStats.max(p._2),
          rddSize,
          missingValues)
        case Categorical => categoricalDistribution(p._1, rdd, missingValues)
      }
    })
    (distributions.map(d => d.name -> d).toMap, basicStats.count)
  }

  private def columnAsDoubleRDDWithoutMissingValues(
      sparkDataFrame: org.apache.spark.sql.DataFrame,
      columnIndex: Int): RDD[Double] = {
    sparkDataFrame.rdd.map(cell2Double(_, columnIndex)).filter(!_.isNaN)
  }

  private def categoricalDistribution(
      structField: StructField,
      rdd: RDD[Double],
      missingValues: Long): CategoricalDistribution = {
    val possibleValues: IndexedSeq[String] = possibleValuesForCategoricalColumn(structField)
    val buckets: IndexedSeq[Double] =
      if (possibleValues.nonEmpty ) 0.0 to possibleValues.size.toDouble by 1.0 else IndexedSeq()
    val counts: Array[Long] = if (buckets.size > 1) rdd.histogram(buckets.toArray) else Array()
    val bucketsLabels: IndexedSeq[String] =
      buckets.take(buckets.size - 1).map(a => possibleValues(a.toInt))
    CategoricalDistribution(
      structField.name,
      s"Categorical distribution for ${structField.name} column",
      missingValues,
      bucketsLabels,
      counts)
  }

  private def continuousDistribution(
      structField: StructField,
      rdd: RDD[Double],
      min: Double,
      max: Double,
      rddSize: Long,
      missingValues: Long): ContinuousDistribution = {
    val columnType = structField.dataType
    val (buckets, counts) = histogram(rdd, min, max, columnType)
    val quartiles = calculateQuartiles(rdd, rddSize, columnType)
    val stats = model.Statistics(
      quartiles.median,
      double2Label(max, columnType),
      double2Label(min, columnType),
      double2Label(rdd.mean(), columnType),
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

  private def possibleValuesForCategoricalColumn(structField: StructField): IndexedSeq[String] =
    structField.dataType match {
      case BooleanType => IndexedSeq(false.toString, true.toString)
      case StringType => IndexedSeq()
      // TODO: Categorical
    }

  private def histogram(
      rdd: RDD[Double],
      min: Double,
      max: Double,
      dataType: DataType): (Seq[String], Seq[Long]) = {
    val steps: Int = numberOfSteps(min, max, dataType)
    val buckets: Array[Double] = customRange(min, max, steps)
    (buckets2Labels(buckets.toList.take(buckets.length - 1), dataType), rdd.histogram(buckets))
  }

  private def numberOfSteps(min: Double, max: Double, dataType: DataType): Int =
    if (max - min < DataFrameReportGenerator.doubleTolerance) {
      1
    } else if (dataType == LongType || dataType == TimestampType) {
      Math.min(max.toLong - min.toLong + 1, DataFrameReportGenerator.defaultBucketsNumber).toInt
    } else {
      DataFrameReportGenerator.defaultBucketsNumber
    }

  private def calculateQuartiles(rdd: RDD[Double], size: Long, columnType: DataType): Quartiles =
    if (size > 0) {
      val sortedRdd = rdd.sortBy(identity).zipWithIndex().map {
        case (v, idx) => (idx, v)
      }
      val secondQuartile: String = double2Label(median(sortedRdd, 0, size), columnType)
      if (size >= 4) {
        val firstQuartile: Double = median(sortedRdd, 0, size / 2)
        val thirdQuartile: Double = median(sortedRdd, size / 2 + size % 2, size)
        val outliers: Array[Double] = findOutliers(rdd, firstQuartile, thirdQuartile)
        Quartiles(
          double2Label(firstQuartile, columnType),
          secondQuartile,
          double2Label(thirdQuartile, columnType),
          outliers.map(double2Label(_, columnType)))
      } else {
        Quartiles(null, secondQuartile, null, Seq())
      }
    } else {
      Quartiles(null, null, null, Seq())
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

  private def row2DoubleVector(row: Row): Vector = {
    Vectors.dense((0 until row.size).map(cell2Double(row, _)).toArray)
  }

  private def cell2Double(row: Row, index: Int): Double =
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
        //    TODO: case categorical
      }
    }

  private def buckets2Labels(buckets: Seq[Double], dataType: DataType): Seq[String] =
    buckets.map(double2Label(_, dataType))

  private def double2Label(d: Double, dataType: DataType): String = dataType match {
    case BooleanType => if (d == 0D) false.toString else true.toString
    case LongType => double2String(d)
    case DoubleType => double2String(d)
    case TimestampType =>
      DateTimeConverter.toString(DateTimeConverter.fromMillis(d.toLong))
    // TODO: categorical
  }

  private def distributionType(dataType: DataType): StatType = dataType match {
    case LongType => Continuous
    case TimestampType => Continuous
    case DoubleType => Continuous
    case StringType => Categorical
    case BooleanType => Categorical
    //    TODO: case categorical
  }

  private def categorical2Double(value: String, possibleValues: List[String]): Double =
    possibleValues.indexOf(value).toDouble

  private def cell2String(row: Row, index: Int): String = {
    val structField: StructField = row.schema.apply(index)
    if (row.isNullAt(index)) {
      null
    } else {
      structField.dataType match {
        case TimestampType => DateTimeConverter.toString(
          DateTimeConverter.fromMillis(row.get(index).asInstanceOf[Timestamp].getTime))
        case DoubleType => double2String(row.getDouble(index))
        case _ => row(index).toString
      }
    }
  }

  private def double2String(d: Double): String = {
    val formatter: DecimalFormat =
      new DecimalFormat("#.######", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    formatter.setRoundingMode(RoundingMode.HALF_UP)
    formatter.format(d)
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

private case class Quartiles(first: String, median: String, third: String, outliers: Seq[String])
