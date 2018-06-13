/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.sparkutils


import scala.concurrent.{Await, Future}
import ai.deepsense.sparkutils.spi.SparkSessionInitializer
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag
import scala.reflect.api.Symbols
import scala.reflect.runtime.universe.TypeTag
import scala.util.Try

import akka.actor.ActorSystem
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, GBTClassificationModel, MultilayerPerceptronClassificationModel, RandomForestClassificationModel}
import org.apache.spark.ml.feature.PCAModel
import org.apache.spark.ml.linalg
import org.apache.spark.ml.regression.{DecisionTreeRegressionModel, GBTRegressionModel, RandomForestRegressionModel}
import org.apache.spark.ml.util.{MLReader, MLWriter}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.{SparkContext, ml}


class SparkSQLSession private[sparkutils](val sparkSession: SparkSession) {
  def this(sparkContext: SparkContext) = this(SparkSession.builder().config(sparkContext.getConf).getOrCreate())

  // Calls SPI to allow registered clients to inject UDFs other global session state.
  SparkSessionInitializer(sparkSession)

  def sparkContext: SparkContext = sparkSession.sparkContext

  def createDataFrame(rdd: RDD[Row], schema: StructType): DataFrame = sparkSession.createDataFrame(rdd, schema)

  def createDataFrame[T <: Product : TypeTag : ClassTag](rdd: RDD[T]): DataFrame = sparkSession.createDataFrame(rdd)

  def createDataFrame[A <: Product : TypeTag](data: Seq[A]): DataFrame = sparkSession.createDataFrame(data)

  def udfRegistration: UDFRegistration = sparkSession.udf

  def table(tableName: String): DataFrame = sparkSession.table(tableName)

  def read: DataFrameReader = sparkSession.read

  def sql(text: String): DataFrame = sparkSession.sql(text)

  def dropTempTable(name: String): Unit = sparkSession.catalog.dropTempView(name)

  def newSession(): SparkSQLSession = new SparkSQLSession(sparkSession.newSession())

  // This is for pyexecutor.py and DOperables and SparkSessionInitializer
  def getSparkSession = sparkSession
}

object SQL {
  def registerTempTable(dataFrame: DataFrame, name: String): Unit = dataFrame.createOrReplaceTempView(name)

  def sparkSQLSession(dataFrame: DataFrame): SparkSQLSession = new SparkSQLSession(dataFrame.sparkSession)

  def union(dataFrame1: DataFrame, dataFrame2: DataFrame): DataFrame = dataFrame1.union(dataFrame2)

  def dataFrameToJsonRDD(dataFrame: DataFrame): RDD[String] = dataFrame.toJSON.rdd

  def createEmptySparkSQLSession(): SparkSQLSession = {
    new SparkSQLSession(SparkSession.builder().getOrCreate().newSession())
  }

  val SqlParser = catalyst.parser.CatalystSqlParser

  type ExceptionThrownByInvalidExpression = org.apache.spark.sql.catalyst.parser.ParseException
}

object Linalg {
  type DenseMatrix = linalg.DenseMatrix
  type Vector = linalg.Vector
  val Vectors = linalg.Vectors
  type VectorUDT = org.apache.spark.hacks.SparkVectors.VectorUDT
}

object ML {

  abstract class Transformer extends ml.Transformer {
    final override def transform(dataset: Dataset[_]): DataFrame = transformDF(dataset.toDF)

    def transformDF(dataFrame: DataFrame): DataFrame
  }

  abstract class Model[T <: ml.Model[T]] extends ml.Model[T] {
    final override def transform(dataset: Dataset[_]): DataFrame = transformDF(dataset.toDF)

    def transformDF(dataFrame: DataFrame): DataFrame
  }

  abstract class Estimator[M <: ml.Model[M]] extends ml.Estimator[M] {
    final override def fit(dataset: Dataset[_]): M = fitDF(dataset.toDF)

    def fitDF(dataFrame: DataFrame): M
  }

  abstract class Evaluator extends ml.evaluation.Evaluator {
    final override def evaluate(dataset: Dataset[_]): Double = evaluateDF(dataset.toDF)

    def evaluateDF(dataFrame: DataFrame): Double
  }

  trait MLReaderWithSparkContext {
    self: MLReader[_] =>
    def sparkContext: SparkContext = sparkSession.sparkContext
  }

  trait MLWriterWithSparkContext {
    self: MLWriter =>
    def sparkContext: SparkContext = sparkSession.sparkContext
  }

  object ModelLoading {
    def decisionTreeClassification(path: String): Option[DecisionTreeClassificationModel] = {
      Some(DecisionTreeClassificationModel.load(path))
    }

    def decisionTreeRegression(path: String): Option[DecisionTreeRegressionModel] = {
      Some(DecisionTreeRegressionModel.load(path))
    }

    def GBTClassification(path: String): Option[GBTClassificationModel] = {
      Some(GBTClassificationModel.load(path))
    }

    def GBTRegression(path: String): Option[GBTRegressionModel] = {
      Some(GBTRegressionModel.load(path))
    }

    def multilayerPerceptronClassification(path: String): Option[MultilayerPerceptronClassificationModel] = {
      Some(MultilayerPerceptronClassificationModel.load(path))
    }

    def randomForestClassification(path: String): Option[RandomForestClassificationModel] = {
      Some(RandomForestClassificationModel.load(path))
    }

    def randomForestRegression(path: String): Option[RandomForestRegressionModel] = {
      Some(RandomForestRegressionModel.load(path))
    }
  }

  object ModelParams {
    def numTreesFromRandomForestRegressionModel(rfModel: RandomForestRegressionModel): Int = rfModel.getNumTrees

    def pcFromPCAModel(pcaModel: PCAModel): Linalg.DenseMatrix = pcaModel.pc
  }

}

object CSV {
  val EscapeQuoteChar = "\\"

  def additionalEscapings(separator: String)(row: String): String = row
}

object PythonGateway {
  val gatewayServerHasCallBackClient: Boolean = false
}

object TypeUtils {
  def isAbstract(c: Symbols#ClassSymbolApi): Boolean = c.isAbstract
}

object AkkaUtils {
  def terminate(as: ActorSystem): Unit = as.terminate()

  def awaitTermination(as: ActorSystem) = Await.result(as.whenTerminated, Duration.Inf)
}

object ScalaUtils {
  def futureFromTry[T](t: Try[T]): Future[T] = Future.fromTry(t)
}
