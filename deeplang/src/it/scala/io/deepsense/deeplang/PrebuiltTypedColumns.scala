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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.dataframe.types.categorical.{CategoriesMapping, MappingMetadataConverter}

trait PrebuiltTypedColumns {
  self: DeeplangIntegTestSupport =>

  import PrebuiltTypedColumns.ExtendedColumnType._
  import PrebuiltTypedColumns.TypedColumn

  protected val targetColumns: Map[ExtendedColumnType, TypedColumn]
  protected val featureColumns: Map[ExtendedColumnType, TypedColumn]

  protected def buildColumns(
      makeName: ExtendedColumnType => String): Map[ExtendedColumnType, TypedColumn] = Map(
    nonBinaryValuedNumeric -> TypedColumn(
      Seq(1.8, 2.2, 3.3),
      StructField(makeName(nonBinaryValuedNumeric), DoubleType)),
    binaryValuedNumeric -> TypedColumn(
      Seq(1.0, 0.0, 1.0),
      StructField(makeName(binaryValuedNumeric), DoubleType)),
    categorical1 -> TypedColumn(
      Seq(0, 0, 0),
      StructField(makeName(categorical1), IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(CategoriesMapping(Seq("A"))))),
    categorical2 -> TypedColumn(
      Seq(0, 1, 0),
      StructField(makeName(categorical2), IntegerType,
        metadata = MappingMetadataConverter.mappingToMetadata(CategoriesMapping(Seq("A", "B"))))),
    categoricalMany -> TypedColumn(
      Seq(0, 1, 2),
      StructField(makeName(categoricalMany), IntegerType,
        metadata =
          MappingMetadataConverter.mappingToMetadata(CategoriesMapping(Seq("A", "B", "C"))))),
    boolean -> TypedColumn(Seq(true, false, true), StructField(makeName(boolean), BooleanType)),
    string -> TypedColumn(Seq("X", "Y", "Z"), StructField(makeName(string), StringType)),
    timestamp -> TypedColumn(
      Seq(DateTimeConverter.now, DateTimeConverter.now, DateTimeConverter.now),
      StructField(makeName(timestamp), TimestampType)))

  protected def makeDataFrame(
      target: ExtendedColumnType, features: ExtendedColumnType*): DataFrame =
    createDataFrame(makeRows(target, features: _*), makeSchema(target, features: _*))

  protected def makeDataFrameOfFeatures(features: ExtendedColumnType*): DataFrame = {
    createDataFrame(makeRowsOfFeatures(features: _*), makeSchemaOfFeatures(features: _*))
  }

  protected def makeRows(target: ExtendedColumnType, features: ExtendedColumnType*): Seq[Row] = {
    val featureColumnsValues =
      featureColumns.filterKeys(features.contains(_)).values.map(_.values).toSeq
    val targetColumnValues = targetColumns(target).values

    makeRowsOfColumnsValues(featureColumnsValues :+ targetColumnValues)
  }

  protected def makeRowsOfFeatures(features: ExtendedColumnType*): Seq[Row] = {
    val featureColumnsValues =
      featureColumns.filterKeys(features.contains(_)).values.map(_.values).toSeq
    makeRowsOfColumnsValues(featureColumnsValues)
  }

  private def makeRowsOfColumnsValues(columnsValues: Seq[Seq[Any]]): Seq[Row] = {
    val rowsCount = columnsValues.head.size
    val z = List.fill[Row](rowsCount)(Row()).toSeq

    columnsValues.foldLeft(z) {
      case (rows: Seq[Row], column: Seq[Any]) =>
        rows zip column map {
          case (row: Row, value: Any) =>
            Row.fromSeq(row.toSeq :+ value)
        }
    }
  }

  protected def makeSchema(
      target: ExtendedColumnType, features: ExtendedColumnType*): StructType = {
    val targetFields = Seq(target) map { targetColumns(_).structField }
    val featureFields = features map { featureColumns(_).structField }
    StructType(featureFields ++ targetFields)
  }

  protected def makeSchemaOfFeatures(features: ExtendedColumnType*): StructType = {
    val featureFields = features map { featureColumns(_).structField }
    StructType(featureFields)
  }
}

object PrebuiltTypedColumns {
  object ExtendedColumnType extends Enumeration {
    type ExtendedColumnType = Value
    val nonBinaryValuedNumeric = Value("nonBinaryValuedNumeric")
    val binaryValuedNumeric = Value("binaryValuedNumeric")
    val boolean = Value("boolean")
    val categorical1 = Value("categorical1")
    val categorical2 = Value("categorical2")
    val categoricalMany = Value("categoricalMany")
    val string = Value("string")
    val timestamp = Value("timestamp")
  }

  case class TypedColumn(values: Seq[Any], structField: StructField)

  import ExtendedColumnType.ExtendedColumnType

  def featureName(feature: ExtendedColumnType): String = feature.toString + "_feature"

  def targetName(target: ExtendedColumnType): String = target.toString + "_target"

  def predictionName(prediction: ExtendedColumnType): String = prediction.toString + "_prediction"
}
