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

package ai.deepsense.deeplang.utils

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.scalatest.Matchers

import ai.deepsense.deeplang.doperables.dataframe.DataFrame

trait DataFrameMatchers extends Matchers {
  def assertDataFramesEqual(
      actualDf: DataFrame,
      expectedDf: DataFrame,
      checkRowOrder: Boolean = true,
      checkNullability: Boolean = true): Unit = {
    // Checks only semantic identity, not objects location in memory
    assertSchemaEqual(
      actualDf.sparkDataFrame.schema,
      expectedDf.sparkDataFrame.schema,
      checkNullability)
    val collectedRows1: Array[Row] = actualDf.sparkDataFrame.collect()
    val collectedRows2: Array[Row] = expectedDf.sparkDataFrame.collect()
    if (checkRowOrder) {
      collectedRows1 shouldBe collectedRows2
    } else {
      collectedRows1 should contain theSameElementsAs collectedRows2
    }
  }

  def assertSchemaEqual(
      actualSchema: StructType,
      expectedSchema: StructType,
      checkNullability: Boolean): Unit = {
    val (actual, expected) = if (checkNullability) {
      (actualSchema, expectedSchema)
    } else {
      val actualNonNull = StructType(actualSchema.map(_.copy(nullable = false)))
      val expectedNonNull = StructType(expectedSchema.map(_.copy(nullable = false)))
      (actualNonNull, expectedNonNull)
    }
    assertSchemaEqual(actual, expected)
  }

  def assertSchemaEqual(actualSchema: StructType, expectedSchema: StructType): Unit = {
    actualSchema.treeString shouldBe expectedSchema.treeString
  }
}

object DataFrameMatchers extends DataFrameMatchers
