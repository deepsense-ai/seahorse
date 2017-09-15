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

package ai.deepsense.seahorse.datasource.db.schema

import java.util.UUID

import ai.deepsense.seahorse.datasource.DatasourceManagerConfig
import ai.deepsense.seahorse.datasource.db.{Database, EnumColumnMapper}
import ai.deepsense.seahorse.datasource.model.DatasourceType.{apply => _, _}
import ai.deepsense.seahorse.datasource.model.FileFormat._
import ai.deepsense.seahorse.datasource.model.{CsvSeparatorType, DatasourceType, FileFormat, Visibility}
import ai.deepsense.commons.service.db.CommonSlickFormats
import ai.deepsense.seahorse.datasource.model.CsvSeparatorType.{apply => _, _}
import ai.deepsense.seahorse.datasource.model.Visibility.{apply => _, _}

object DatasourcesSchema {

  import Database.api._
  import CommonSlickFormats._

  case class DatasourceDB(
    generalParameters: DatasourceDBGeneralParameters,
    jdbcParameters: DatasourceDBJdbcParameters,
    fileParameters: DatasourceDBFileParameters,
    googleParameters: DatasourceDBGoogleParameters)

  case class DatasourceDBGeneralParameters(
    id: UUID,
    ownerId: UUID,
    ownerName: String,
    name: String,
    creationDateTime: java.util.Date,
    visibility: Visibility,
    downloadUri: Option[String],
    datasourceType: DatasourceType)

  case class DatasourceDBGoogleParameters(
      googleSpreadsheetId: Option[String],
      googleServiceAccountCredentials: Option[String],
      googleSpreadsheetIncludeHeader: Option[Boolean],
      googleSpreadsheetConvert01ToBoolean: Option[Boolean])

  case class DatasourceDBJdbcParameters(
      jdbcUrl: Option[String],
      jdbcDriver: Option[String],
      jdbcTable: Option[String],
      jdbcQuery: Option[String])

  case class DatasourceDBFileParameters(
      externalFileUrl: Option[String],
      hdfsPath: Option[String],
      libraryPath: Option[String],
      fileFormat: Option[FileFormat],
      fileCsvIncludeHeader: Option[Boolean],
      fileCsvConvert01ToBoolean: Option[Boolean],
      fileCsvSeparatorType: Option[CsvSeparatorType],
      fileCsvCustomSeparator: Option[String])


  implicit val datasourceTypeFormat = EnumColumnMapper(DatasourceType)
  implicit val fileFormatFormat = EnumColumnMapper(FileFormat)
  implicit val visibilityFormat = EnumColumnMapper(Visibility)
  implicit val csvSeparatorType = EnumColumnMapper(CsvSeparatorType)

  implicit val javaUtilDateMapper =
    MappedColumnType.base[java.util.Date, java.sql.Timestamp] (
      d => new java.sql.Timestamp(d.getTime),
      d => new java.util.Date(d.getTime))

  final class DatasourceTable(tag: Tag)
      extends Table[DatasourceDB](tag, Some(DatasourceManagerConfig.database.schema), "datasource") {
    def id = column[UUID]("id", O.PrimaryKey)
    def ownerId = column[UUID]("ownerId")
    def ownerName = column[String]("ownerName")
    def name = column[String]("name")
    def creationDateTime = column[java.util.Date]("creationDateTime")
    def visibility = column[Visibility]("visibility")
    def downloadUri = column[Option[String]]("downloadUri")
    def datasourceType = column[DatasourceType]("datasourceType")
    def jdbcUrl = column[Option[String]]("jdbcUrl")
    def jdbcDriver = column[Option[String]]("jdbcDriver")
    def jdbcTable = column[Option[String]]("jdbcTable")
    def jdbcQuery = column[Option[String]]("jdbcQuery")
    def hdfsPath = column[Option[String]]("hdfsPath")
    def libraryPath = column[Option[String]]("libraryPath")
    def externalFileUrl = column[Option[String]]("externalFileUrl")
    def fileFormat = column[Option[FileFormat]]("fileFormat")
    def fileCsvIncludeHeader = column[Option[Boolean]]("fileCsvIncludeHeader")
    def fileCsvConvert01ToBoolean = column[Option[Boolean]]("fileCsvConvert01ToBoolean")
    def fileCsvSeparatorType = column[Option[CsvSeparatorType]]("fileCsvSeparatorType")
    def fileCsvCustomSeparator = column[Option[String]]("fileCsvSeparator")
    def googleSpreadsheetId = column[Option[String]]("googleSpreadsheetId")
    def googleServiceAccountCredentials = column[Option[String]]("googleServiceAccountCredentials")
    def googleSpreadsheetIncludeHeader = column[Option[Boolean]]("googleSpreadsheetIncludeHeader")
    def googleSpreadsheetConvert01ToBoolean = column[Option[Boolean]]("googleSpreadsheetConvert01ToBoolean")

    def * = (
      (id, ownerId, ownerName, name, creationDateTime, visibility, downloadUri, datasourceType),
      (jdbcUrl, jdbcDriver, jdbcTable, jdbcQuery),
      (externalFileUrl, hdfsPath, libraryPath, fileFormat, fileCsvIncludeHeader,
        fileCsvConvert01ToBoolean, fileCsvSeparatorType, fileCsvCustomSeparator),
      (googleSpreadsheetId, googleServiceAccountCredentials,
        googleSpreadsheetIncludeHeader, googleSpreadsheetConvert01ToBoolean)
    ) <> ({ x: ((UUID, UUID, String, String, java.util.Date, Visibility, Option[String], DatasourceType),
                (Option[String], Option[String], Option[String], Option[String]),
                (Option[String], Option[String], Option[String], Option[FileFormat], Option[Boolean],
                  Option[Boolean], Option[CsvSeparatorType], Option[String]),
                (Option[String], Option[String], Option[Boolean], Option[Boolean])) =>
       x match {
         case (generalParameters, jdbcParameters, fileParameters, googleParameters) =>
      DatasourceDB(
        DatasourceDBGeneralParameters.tupled(generalParameters),
        DatasourceDBJdbcParameters.tupled(jdbcParameters),
        DatasourceDBFileParameters.tupled(fileParameters),
        DatasourceDBGoogleParameters.tupled(googleParameters))
    }}, { x: DatasourceDB => x match {
      case DatasourceDB(generalParameteres, jdbcParameters, fileParameters, googleParameters) =>
        for {
          general <- DatasourceDBGeneralParameters.unapply(generalParameteres)
          jdbc <- DatasourceDBJdbcParameters.unapply(jdbcParameters)
          file <- DatasourceDBFileParameters.unapply(fileParameters)
          google <- DatasourceDBGoogleParameters.unapply(googleParameters)
        } yield (general, jdbc, file, google)
    }})

  }

  lazy val datasourcesTable = TableQuery[DatasourceTable]
}

// sbt-native-package won't work with multiple Mains
// https://github.com/sbt/sbt-native-packager/pull/319
// TODO use sbt-assembly and define mainClass in assembly as it's solved in Neptune
/*
object PrintDDL extends App {
  import Database.api._
  import DatasourcesSchema._
  // scalastyle:off println
  println(datasourcesTable.schema.createStatements.mkString("\n"))
  // scalastyle:on println
}
*/
