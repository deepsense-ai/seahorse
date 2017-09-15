/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.workflowexecutor

import java.io.{InputStream, Serializable}

import scala.collection.mutable
import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

import akka.actor.ActorSystem
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import io.deepsense.commons.spark.sql.UserDefinedFunctions
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.deeplang._
import io.deepsense.models.entities.Entity
import io.deepsense.models.workflows.{ExecutionReport, WorkflowWithVariables}
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.{GraphFinished, Launch}

/**
 * WorkflowExecutor creates an execution context and then executes a workflow on Spark.
 */
case class WorkflowExecutor(
    workflow: WorkflowWithVariables,
    generateReports: Boolean)
  extends Logging {

  val dOperableCache = mutable.Map[Entity.Id, DOperable]()
  private val actorSystemName = "WorkflowExecutor"

  def execute(): Try[ExecutionReport] = {
    val executionContext = createExecutionContext()

    val actorSystem = ActorSystem(actorSystemName)
    val workflowExecutorActor = actorSystem.actorOf(WorkflowExecutorActor.props(executionContext))

    val resultPromise: Promise[GraphFinished] = Promise()
    workflowExecutorActor ! Launch(workflow.graph, generateReports, resultPromise)

    logger.info("Awaiting execution end...")
    actorSystem.awaitTermination()

    val report = resultPromise.future.value.get match {
      case Failure(exception) => // WEA failed with an exception
        logger.error("WEA failed: ", exception)
        throw exception
      case Success(GraphFinished(graph, entitiesMap)) =>
        logger.info(s"WEA finished successfully: ${workflow.graph}")
        Try(ExecutionReport(
          graph.state.status,
          graph.state.error,
          graph.nodeById.mapValues(_.state),
          entitiesMap
        ))
    }

    cleanup(actorSystem, executionContext)
    report
  }

  private def createExecutionContext(): ExecutionContext = {
    val dOperableCatalog = new DOperableCatalog
    CatalogRecorder.registerDOperables(dOperableCatalog)
    val executionContext = new ExecutionContext(dOperableCatalog)

    executionContext.sparkContext = createSparkContext()
    executionContext.sqlContext = createSqlContext(executionContext.sparkContext)
    executionContext.dataFrameBuilder = DataFrameBuilder(executionContext.sqlContext)
    executionContext.fsClient = FileSystemClientStub() // TODO temporarily mocked
    executionContext.entityStorageClient = null // Not used
    executionContext
  }

  private def createSparkContext(): SparkContext = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("Seahorse Workflow Executor")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .registerKryoClasses(Array())

    new SparkContext(sparkConf)
  }

  private def createSqlContext(sparkContext: SparkContext): SQLContext = {
    val sqlContext = new SQLContext(sparkContext)
    UserDefinedFunctions.registerFunctions(sqlContext.udf)
    sqlContext
  }

  private def cleanup(actorSystem: ActorSystem, executionContext: ExecutionContext): Unit = {
    logger.debug("Cleaning up...")
    actorSystem.shutdown()
    logger.debug("Akka terminated!")
    executionContext.sparkContext.stop()
    logger.debug("Spark terminated!")
  }
}

private case class FileSystemClientStub() extends FileSystemClient {
  override def copyLocalFile[T <: Serializable]
  (localFilePath: String, remoteFilePath: String): Unit = ()

  override def delete(path: String): Unit = ()

  override def saveObjectToFile[T <: Serializable](path: String, instance: T): Unit = ()

  override def fileExists(path: String): Boolean = throw new UnsupportedOperationException

  override def saveInputStreamToFile(
    inputStream: InputStream, destinationPath: String): Unit = ()

  override def getFileInfo(path: String): Option[FileInfo] = throw new UnsupportedOperationException

  override def readFileAsObject[T <: Serializable](path: String): T =
    throw new UnsupportedOperationException
}
