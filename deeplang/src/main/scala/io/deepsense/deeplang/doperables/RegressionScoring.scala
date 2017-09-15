/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.regression.GeneralizedLinearModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame

trait RegressionScoring {

  def scoreRegression(context: ExecutionContext)(
      dataFrame: DataFrame,
      featureColumns: Seq[String],
      targetColumn: String,
      labelColumnSuffix: String,
      featuresTransformer: RDD[Vector] => RDD[Vector],
      model: GeneralizedLinearModel): DataFrame = {

    val vectors: RDD[Vector] = dataFrame.toSparkVectorRDD(featureColumns)
    val transformedVectors = featuresTransformer(vectors)
    transformedVectors.cache()
    val predictionsRDD: RDD[Double] = model.predict(transformedVectors)

    val uniqueLabelColumnName = dataFrame.uniqueColumnName(targetColumn, labelColumnSuffix)
    val outputSchema = StructType(dataFrame.sparkDataFrame.schema.fields :+
      StructField(uniqueLabelColumnName, DoubleType))

    val outputRDD = dataFrame.sparkDataFrame.rdd.zip(predictionsRDD).map({
      case (row, prediction) => Row.fromSeq(row.toSeq :+ prediction)})

    context.dataFrameBuilder.buildDataFrame(outputSchema, outputRDD)
  }
}
