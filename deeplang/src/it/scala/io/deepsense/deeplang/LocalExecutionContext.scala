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

import java.util.UUID

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar._

import io.deepsense.api.datasourcemanager.model.Datasource
import io.deepsense.commons.rest.client.datasources.{DatasourceClient, DatasourceClientFactory}
import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.sparkutils.SparkSQLSession

class DatasourceMock extends DatasourceClientFactory with DatasourceClient{
  override def createClient: DatasourceClient = new DatasourceMock
  override def toFactory: DatasourceClientFactory = new DatasourceMock
  override def getDatasource(uuid: UUID): Option[Datasource] = None
}

trait LocalExecutionContext { self: BeforeAndAfterAll =>
  var commonExecutionContext: CommonExecutionContext = _
  implicit var executionContext: ExecutionContext = _

  val sparkConf: SparkConf = DeeplangIntegTestSupport.sparkConf
  val sparkContext: SparkContext = DeeplangIntegTestSupport.sparkContext
  val sparkSQLSession: SparkSQLSession = DeeplangIntegTestSupport.sparkSQLSession

  val dOperableCatalog = CatalogRecorder.catalogs.dOperableCatalog

  protected override def beforeAll(): Unit = {
    commonExecutionContext = prepareCommonExecutionContext()
    executionContext = prepareExecutionContext()
  }

  protected def prepareCommonExecutionContext(): CommonExecutionContext = {
    val inferContext = InferContext(
      DataFrameBuilder(sparkSQLSession),
      "testTenantId",
      dOperableCatalog,
      mock[InnerWorkflowParser])

    new MockedCommonExecutionContext(
      sparkContext,
      sparkSQLSession,
      inferContext,
      ExecutionMode.Batch,
      LocalFileSystemClient(),
      "testTenantId",
      mock[InnerWorkflowExecutor],
      mock[DataFrameStorage],
      None,
      None,
      new DatasourceMock,
      mock[CustomCodeExecutionProvider])
  }

  protected def prepareExecutionContext(): ExecutionContext = {
    val inferContext = InferContext(
      DataFrameBuilder(sparkSQLSession),
      "testTenantId",
      dOperableCatalog,
      mock[InnerWorkflowParser])

    new MockedExecutionContext(
      sparkContext,
      sparkSQLSession,
      inferContext,
      ExecutionMode.Batch,
      LocalFileSystemClient(),
      "testTenantId",
      mock[InnerWorkflowExecutor],
      mock[ContextualDataFrameStorage],
      None,
      None,
      new DatasourceMock,
      new MockedContextualCodeExecutor)
  }

}
