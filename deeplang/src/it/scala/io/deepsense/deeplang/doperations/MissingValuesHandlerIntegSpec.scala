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

package io.deepsense.deeplang.doperations

import scala.collection.JavaConverters._

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.parameters.{IndexRangeColumnSelection, MultipleColumnSelection}

class MissingValuesHandlerIntegSpec extends DeeplangIntegTestSupport
  with GeneratorDrivenPropertyChecks
  with Matchers {

  "MissingValuesHandler" should {
    "remove rows with empty values while using REMOVE_ROW strategy" in {
      val values = Seq(
        Row(1, null),
        Row(2, null),
        Row(null, 3),
        Row(4, 4),
        Row(5, 5),
        Row(null, null))

      val df = createDataFrame(values, createSchema)
      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(0))))
      val resultDf = executeOperation(
        MissingValuesHandler(columnSelection, MissingValuesHandler.Strategy.REMOVE_ROW), df)

      val expectedRows = List(
        Row(1, null),
        Row(2, null),
        Row(4, 4),
        Row(5, 5)
      )
      resultDf.sparkDataFrame.collectAsList().asScala.toList shouldBe expectedRows
    }
  }

  private def createSchema: StructType = {
    StructType(List(
      StructField("value1", IntegerType, nullable = true),
      StructField("value2", IntegerType, nullable = true)
    ))
  }

}
