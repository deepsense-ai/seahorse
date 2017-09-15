/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.feature.StandardScalerModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.RidgeRegressionModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DMethod1To1, ExecutionContext}
import io.deepsense.reportlib.model.ReportContent

case class TrainedRidgeRegression(
    model: Option[RidgeRegressionModel],
    featureColumns: Option[Seq[String]],
    targetColumn: Option[String],
    scaler: Option[StandardScalerModel])
  extends RidgeRegression
  with Scorable {

  def this() = this(None, None, None, None)

  override val score = new DMethod1To1[Unit, DataFrame, DataFrame] {

    override def apply(context: ExecutionContext)(p: Unit)(dataframe: DataFrame): DataFrame = {
      val vectors: RDD[Vector] = dataframe.toSparkVectorRDD(featureColumns.get)
      val scaledVectors = scaler.get.transform(vectors)
      val predictionsRDD: RDD[Double] = model.get.predict(scaledVectors)

      val uniqueLabelColumnName = dataframe.uniqueColumnName(
        targetColumn.get, TrainedRidgeRegression.labelColumnSuffix)
      val outputSchema = StructType(dataframe.sparkDataFrame.schema.fields :+
        StructField(uniqueLabelColumnName, DoubleType))

      val outputRDD = dataframe.sparkDataFrame.rdd.zip(predictionsRDD).map({
        case (row, prediction) => Row.fromSeq(row.toSeq :+ prediction)})

      context.dataFrameBuilder.buildDataFrame(outputSchema, outputRDD)
    }
  }

  override def report: Report = Report(ReportContent("Report for TrainedRidgeRegression.\n" +
    s"Feature columns: ${featureColumns.get.mkString(", ")}\n" +
    s"Target column: ${targetColumn.get}\n" +
    s"Model: $model"))
}

object TrainedRidgeRegression {
  val labelColumnSuffix = "prediction"
}
