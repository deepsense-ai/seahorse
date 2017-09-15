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

package ai.deepsense.deeplang.doperations.readwritedataframe.filestorage.csv

import java.sql.Timestamp

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.exceptions.UnsupportedColumnTypeException

/**
  * In CSV there are no type hints/formats. Everything is plain text between separators.
  *
  * That's why it's needed to convert all fields to string and make sure that there are no
  * nested structures like Maps or Arrays.
  */
object CsvSchemaStringifierBeforeCsvWriting {

  def preprocess(dataFrame: DataFrame)
                (implicit context: ExecutionContext): DataFrame = {
    requireNoComplexTypes(dataFrame)

    val schema = dataFrame.sparkDataFrame.schema
    def stringifySelectedTypes(schema: StructType): StructType = {
      StructType(
        schema.map {
          case field: StructField => field.copy(dataType = StringType)
        }
      )
    }

    context.dataFrameBuilder.buildDataFrame(
      stringifySelectedTypes(schema),
      dataFrame.sparkDataFrame.rdd.map(stringifySelectedCells(schema)))
  }

  private def requireNoComplexTypes(dataFrame: DataFrame): Unit = {
    dataFrame.sparkDataFrame.schema.fields.map(structField =>
      (structField.dataType, structField.name)
    ).foreach {
      case (dataType, columnName) => dataType match {
        case _: ArrayType | _: MapType | _: StructType =>
          throw UnsupportedColumnTypeException(columnName, dataType)
        case _ => ()
      }
    }

  }

  private def stringifySelectedCells(originalSchema: StructType)(row: Row): Row = {
    Row.fromSeq(
      row.toSeq.zipWithIndex map { case (value, index) =>
        (value, originalSchema(index).dataType) match {
          case (null, _) => ""
          case (_, BooleanType) =>
            if (value.asInstanceOf[Boolean]) "1" else "0"
          case (_, TimestampType) =>
            DateTimeConverter.toString(
              DateTimeConverter.fromMillis(value.asInstanceOf[Timestamp].getTime))
          case (x, _) => value.toString
        }
      })
  }

}
