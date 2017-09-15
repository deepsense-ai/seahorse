/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang

import org.apache.spark.sql
import org.apache.spark.sql.types.StructType
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar

import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.inference.InferContext

trait DeeplangTestSupport extends MockitoSugar {

  protected def createInferContext(
      dOperableCatalog: DOperableCatalog): InferContext = MockedInferContext(dOperableCatalog)

  protected def createExecutionContext: ExecutionContext = {
    val mockedExecutionContext = mock[ExecutionContext]
    val mockedInferContext = mock[InferContext]
    when(mockedExecutionContext.inferContext).thenReturn(mockedInferContext)
    mockedExecutionContext
  }

  protected def createSchema(fields: Array[String] = Array[String]()): StructType = {
    val schemaMock = mock[StructType]
    when(schemaMock.fieldNames).thenReturn(fields)
    schemaMock
  }

  protected def createSparkDataFrame(schema: StructType = createSchema()) = {
    val sparkDataFrameMock = mock[sql.DataFrame]
    when(sparkDataFrameMock.schema).thenReturn(schema)
    when(sparkDataFrameMock.toDF).thenReturn(sparkDataFrameMock)
    sparkDataFrameMock
  }

  protected def createDataFrame(fields: Array[String] = Array[String]()): DataFrame = {
    val schema = createSchema(fields)
    createDataFrame(schema)
  }

  protected def createDataFrame(schema: StructType): DataFrame = {
    val sparkDataFrameMock = createSparkDataFrame(schema)
    val dataFrameMock = mock[DataFrame]
    when(dataFrameMock.sparkDataFrame).thenReturn(sparkDataFrameMock)
    when(dataFrameMock.schema).thenReturn(Some(schema))
    dataFrameMock
  }
}
