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

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

class SqlCombineSpec extends DeeplangIntegTestSupport {

  import DeeplangIntegTestSupport._

  private val leftName = "left"
  private val leftData = Seq(
    Row("c", 5.0, true),
    Row("a", 5.0, null),
    Row("b", null, false),
    Row(null, 2.1, true)
  )
  private val (firstLeftColumn, secondLeftColumn, thirdLeftColumn) = ("left1", "left2", "left3")
  private val leftSchema = StructType(Seq(
    StructField(firstLeftColumn, StringType),
    StructField(secondLeftColumn, DoubleType),
    StructField(thirdLeftColumn, BooleanType)
  ))
  private val leftDf = createDataFrame(leftData, leftSchema)
  private val rightName = "right"
  private val rightData = Seq(
    Row(5.0, "x"),
    Row(null, "y"),
    Row(null, null)
  )
  private val (firstRightColumn, secondRightColumn) = ("right1", "right2")
  private val rightSchema = StructType(Seq(
    StructField(firstRightColumn, DoubleType),
    StructField(secondRightColumn, StringType)
  ))
  private val rightDf = createDataFrame(rightData, rightSchema)

  "SqlCombine" should {
    "recognize left dataframe name" in {
      val expression = s"SELECT * FROM $leftName"
      val result = executeSqlCombine(expression, leftName, leftDf, rightName, rightDf)
      assertDataFramesEqual(result, leftDf)
    }
    "recognize right dataFrame name" in {
      val expression = s"SELECT * FROM $rightName"
      val result = executeSqlCombine(expression, leftName, leftDf, rightName, rightDf)
      assertDataFramesEqual(result, rightDf)
    }
    "allow an arbitrary operation using both dataFrames" in {
      val expression =
        s"""SELECT l.$firstLeftColumn AS first_letter,
            |r.$secondRightColumn AS second_letter
            |FROM left l INNER JOIN right r ON l.$secondLeftColumn = r.$firstRightColumn
         """.stripMargin
      val result = executeSqlCombine(expression, leftName, leftDf, rightName, rightDf)
      val expectedData = Seq(
        Row("c", "x"),
        Row("a", "x")
      )
      val expectedSchema = StructType(Seq(
        StructField("first_letter", StringType),
        StructField("second_letter", StringType)
      ))
      val expected = createDataFrame(expectedData, expectedSchema)
      assertDataFramesEqual(result, expected)
    }
  }

  private def executeSqlCombine(expresssion: String,
                                leftName: String, leftData: DataFrame,
                                rightName: String, rightData: DataFrame): DataFrame = {
    val combine = new SqlCombine()
      .setLeftTableName(leftName)
      .setRightTableName(rightName)
      .setSqlCombineExpression(expresssion)

    executeOperation(combine, leftData, rightData)
  }
}
