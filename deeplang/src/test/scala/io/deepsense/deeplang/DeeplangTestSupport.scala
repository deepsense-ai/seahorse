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

package io.deepsense.deeplang

import org.apache.spark.sql
import org.apache.spark.sql.types.StructType
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.inference.InferContext

trait DeeplangTestSupport extends MockitoSugar {

  protected def createInferContext(
      dOperableCatalog: DOperableCatalog,
      fullInference: Boolean = true): InferContext =
    InferContext(
      mock[DataFrameBuilder],
      "testTenantId",
      dOperableCatalog,
      mock[InnerWorkflowParser],
      fullInference = fullInference)

  protected def createSchema(fields: Array[String] = Array[String]()): StructType = {
    val schemaMock = mock[StructType]
    when(schemaMock.fieldNames).thenReturn(fields)
    schemaMock
  }

  protected def createSparkDataFrame(schema: StructType = createSchema()) = {
    val sparkDataFrameMock = mock[sql.DataFrame]
    when(sparkDataFrameMock.schema).thenReturn(schema)
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
