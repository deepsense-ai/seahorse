/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.sparkutils


import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, GBTClassificationModel, MultilayerPerceptronClassificationModel, RandomForestClassificationModel}
import org.apache.spark.ml.feature.PCAModel
import org.apache.spark.ml.regression.{DecisionTreeRegressionModel, GBTRegressionModel, RandomForestRegressionModel}
import org.apache.spark.ml.util.{MLReader, MLWriter}
import org.apache.spark.mllib.linalg
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.types._
import org.apache.spark.{SparkContext, ml}
import org.apache.spark.sql.hive.HiveContext

class SparkSQLSession private[sparkutils](val sQLContext: SQLContext) {
  def this(sparkContext: SparkContext) = this(new HiveContext(sparkContext))

  def sparkContext: SparkContext = sQLContext.sparkContext
  def createDataFrame(rdd: RDD[Row], schema: StructType): DataFrame = sQLContext.createDataFrame(rdd, schema)
  def createDataFrame[T <: Product : TypeTag : ClassTag](rdd: RDD[T]): DataFrame = sQLContext.createDataFrame(rdd)
  def createDataFrame[A <: Product : TypeTag](data: Seq[A]): DataFrame = sQLContext.createDataFrame(data)
  def udfRegistration: UDFRegistration = sQLContext.udf
  def table(tableName: String): DataFrame = sQLContext.table(tableName)
  def read: DataFrameReader = sQLContext.read
  def sql(text: String): DataFrame = sQLContext.sql(text)
  def dropTempTable(name: String): Unit = sQLContext.dropTempTable(name)
  def newSession(): SparkSQLSession = new SparkSQLSession(sQLContext.newSession())
}

object SQL {
  def registerTempTable(dataFrame: DataFrame, name: String): Unit = dataFrame.registerTempTable(name)
  def sparkSQLSession(dataFrame: DataFrame): SparkSQLSession = new SparkSQLSession(dataFrame.sqlContext)
  def union(dataFrame1: DataFrame, dataFrame2: DataFrame): DataFrame = dataFrame1.unionAll(dataFrame2)
  def createEmptySparkSQLSession(): SparkSQLSession = new SparkSQLSession(SQLContext.getOrCreate(null)).newSession()
  def dataFrameToJsonRDD(dataFrame: DataFrame): RDD[String] = dataFrame.toJSON

  object SqlParser {
    def parseExpression(expr: String): Expression = catalyst.SqlParser.parseExpression(expr)
  }

  type ExceptionThrownByInvalidExpression = org.apache.spark.sql.AnalysisException
}

object Linalg {
  type DenseMatrix = linalg.DenseMatrix
  type Vector = linalg.Vector
  val Vectors = linalg.Vectors
  type VectorUDT = linalg.VectorUDT
}

object ML {
  abstract class Transformer extends ml.Transformer {
    final override def transform(dataset: DataFrame): DataFrame = transformDF(dataset)
    def transformDF(dataFrame: DataFrame): DataFrame
  }
  abstract class Model[T <: ml.Model[T]] extends ml.Model[T] {
    final override def transform(dataset: DataFrame): DataFrame = transformDF(dataset)
    def transformDF(dataFrame: DataFrame): DataFrame
  }
  abstract class Estimator[M <: ml.Model[M]] extends ml.Estimator[M] {
    final override def fit(dataset: DataFrame): M = fitDF(dataset)
    def fitDF(dataFrame: DataFrame): M
  }
  abstract class Evaluator extends ml.evaluation.Evaluator {
    final override def evaluate(dataset: DataFrame): Double = evaluateDF(dataset)
    def evaluateDF(dataFrame: DataFrame): Double
  }

  trait MLReaderWithSparkContext { self: MLReader[_] =>
    def sparkContext: SparkContext = sqlContext.sparkContext
  }
  trait MLWriterWithSparkContext { self: MLWriter =>
    def sparkContext: SparkContext = sqlContext.sparkContext
  }

  object ModelLoading {
    def decisionTreeClassification(path: String): Option[DecisionTreeClassificationModel] = None
    def decisionTreeRegression(path: String): Option[DecisionTreeRegressionModel] = None
    def GBTClassification(path: String): Option[GBTClassificationModel] = None
    def GBTRegression(path: String): Option[GBTRegressionModel] = None
    def multilayerPerceptronClassification(path: String): Option[MultilayerPerceptronClassificationModel] = None
    def randomForestClassification(path: String): Option[RandomForestClassificationModel] = None
    def randomForestRegression(path: String): Option[RandomForestRegressionModel] = None
  }

  object ModelParams {
    def numTreesFromRandomForestRegressionModel(rfModel: RandomForestRegressionModel): Int = rfModel.numTrees

    def pcFromPCAModel(pcaModel: PCAModel): Linalg.DenseMatrix = pcaModel.pc
  }
}

object CSV {
  val EscapeQuoteChar = "\""

  // this is something that Spark-CSV writer does.
  // It's compliant with CSV standard, although unnecessary
  def additionalEscapings(separator: String)(row: String) : String = {
    if (row.startsWith(separator)) {
      "\"\"" + row
    } else {
      row
    }
  }
}

object PythonGateway {
  val gatewayServerHasCallBackClient: Boolean = true
}

