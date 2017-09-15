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

package io.deepsense.deeplang.doperables

import scala.language.reflectiveCalls
import scala.reflect.runtime.universe._

import org.apache.spark.ml.{Transformer => SparkTransformer}
import org.apache.spark.mllib.linalg.{DenseVector, VectorUDT, Vectors}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.exceptions.SparkTransformSchemaException
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers
import io.deepsense.deeplang.{ExecutionContext, TypeUtils}

/**
 * This class creates a Deeplang MultiColumnTransformer from a Spark ML Transformer
 * that has inputCol and outputCol parameters.
 * We assume that every Spark Transformer has a no-arg constructor.
 *
 * @tparam T Wrapped Spark Transformer type
 */
abstract class SparkTransformerAsMultiColumnTransformer[T <: SparkTransformer {
    def setInputCol(value: String): T
    def setOutputCol(value: String): T
  }](implicit tag: TypeTag[T])
  extends MultiColumnTransformer
  with ParamsWithSparkWrappers {

  lazy val sparkTransformer: T = TypeUtils.instanceOfType(tag)

  /**
   * Determines whether to perform automatic conversion of numeric input column
   * to one-element vector column. Can be overridden in `Transformer` implementation.
   */
  def convertInputNumericToVector: Boolean = false

  override protected def getSpecificParams: Array[Param[_]] = Array()

  private def updateSchema(schema: StructType, colName: String, dataType: DataType): StructType = {
    updateSchema(schema, schema.fieldIndex(colName), dataType)
  }

  private def updateSchema(schema: StructType, idx: Int, dataType: DataType): StructType = {
    schema.copy(schema.fields.clone().updated(idx, schema(idx).copy(dataType = dataType)))
  }

  private def isColumnNumeric(schema: StructType, colName: String): Boolean = {
    schema(colName).dataType.isInstanceOf[NumericType]
  }

  private def convertAndTransformDataFrame(
      inputColumn: String,
      outputColumn: String,
      context: ExecutionContext,
      dataFrame: DataFrame,
      transformer: T): org.apache.spark.sql.DataFrame = {
    val inputColumnIdx = dataFrame.schema.get.fieldIndex(inputColumn)
    val convertedRdd = dataFrame.sparkDataFrame.map { r =>
      Row.fromSeq(r.toSeq.updated(inputColumnIdx, Vectors.dense(r.getDouble(inputColumnIdx))))
    }
    val convertedSchema = updateSchema(dataFrame.schema.get, inputColumn, new VectorUDT())
    val convertedDf = context.sqlContext.createDataFrame(convertedRdd, convertedSchema)

    val transformed = transformer.transform(convertedDf)

    val transformedRdd = transformed.rdd.map { r =>
      Row.fromSeq(
        r.toSeq.updated(inputColumnIdx, r.get(inputColumnIdx).asInstanceOf[DenseVector].apply(0)))
    }

    context.sqlContext.createDataFrame(
      transformedRdd,
      transformSingleColumnSchema(inputColumn, outputColumn, dataFrame.schema.get).get)
  }

  override def transformSingleColumn(
      inputColumn: String,
      outputColumn: String,
      context: ExecutionContext,
      dataFrame: DataFrame): DataFrame = {
    val transformer = sparkTransformerWithParams(dataFrame.sparkDataFrame.schema)
    transformer.setInputCol(inputColumn)
    transformer.setOutputCol(outputColumn)
    if (convertInputNumericToVector && isColumnNumeric(dataFrame.schema.get, inputColumn)) {
      // Automatically convert numeric input column to one-element vector column
      val transformedDf =
        convertAndTransformDataFrame(inputColumn, outputColumn, context, dataFrame, transformer)
      DataFrame.fromSparkDataFrame(transformedDf)
    } else {
      // Input column type is vector
      DataFrame.fromSparkDataFrame(transformer.transform(dataFrame.sparkDataFrame))
    }
  }

  override def transformSingleColumnSchema(
      inputColumn: String,
      outputColumn: String,
      schema: StructType): Option[StructType] = {
    val transformer = sparkTransformerWithParams(schema)
    transformer.setInputCol(inputColumn)
    transformer.setOutputCol(outputColumn)
    try {
      if (convertInputNumericToVector && isColumnNumeric(schema, inputColumn)) {
        // Automatically convert numeric input column to one-element vector column
        val convertedSchema = updateSchema(schema, inputColumn, new VectorUDT())
        val transformedSchema = transformer.transformSchema(convertedSchema)
        Some(updateSchema(transformedSchema, inputColumn, DoubleType))
      } else {
        // Input column type is vector
        val transformedSchema = transformer.transformSchema(schema)
        Some(transformedSchema)
      }
    } catch {
      case e: Exception => throw new SparkTransformSchemaException(e)
    }
  }

  private def sparkTransformerWithParams(schema: StructType) =
    sparkTransformer.copy(sparkParamMap(sparkTransformer, schema)).asInstanceOf[T]
}
