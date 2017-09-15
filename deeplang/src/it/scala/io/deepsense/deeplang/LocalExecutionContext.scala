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

package io.deepsense.deeplang

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar._

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.deeplang.inference.InferContext

trait LocalExecutionContext { self: BeforeAndAfterAll =>

  var commonExecutionContext: CommonExecutionContext = _
  implicit var executionContext: ExecutionContext = _

  val sparkConf: SparkConf = DeeplangIntegTestSupport.sparkConf
  val sparkContext: SparkContext = DeeplangIntegTestSupport.sparkContext
  val sparkSession: SparkSession = DeeplangIntegTestSupport.sparkSession

  val dOperableCatalog = {
    val catalog = new DOperableCatalog
    CatalogRecorder.registerDOperables(catalog)
    catalog
  }

  protected override def beforeAll(): Unit = {
    commonExecutionContext = prepareCommonExecutionContext()
    executionContext = prepareExecutionContext()
  }

  protected def prepareCommonExecutionContext(): CommonExecutionContext = {
    val inferContext = InferContext(
      DataFrameBuilder(sparkSession),
      "testTenantId",
      dOperableCatalog,
      mock[InnerWorkflowParser])

    new MockedCommonExecutionContext(
      sparkContext,
      sparkSession,
      inferContext,
      LocalFileSystemClient(),
      "testTenantId",
      mock[InnerWorkflowExecutor],
      mock[DataFrameStorage],
      mock[CustomCodeExecutionProvider])
  }

  protected def prepareExecutionContext(): ExecutionContext = {
    val inferContext = InferContext(
      DataFrameBuilder(sparkSession),
      "testTenantId",
      dOperableCatalog,
      mock[InnerWorkflowParser])

    new MockedExecutionContext(
      sparkContext,
      sparkSession,
      inferContext,
      LocalFileSystemClient(),
      "testTenantId",
      mock[InnerWorkflowExecutor],
      mock[ContextualDataFrameStorage],
      new MockedContextualCodeExecutor)
  }

}
