/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db.schema

import java.util.UUID

import io.deepsense.seahorse.datasource.DatasourceManagerConfig
import io.deepsense.seahorse.datasource.db.{Database, EnumColumnMapper}
import io.deepsense.seahorse.datasource.model.DatasourceType.{apply => _, _}
import io.deepsense.seahorse.datasource.model.FileFormat._
import io.deepsense.seahorse.datasource.model.{CsvSeparatorType, DatasourceType, FileFormat, Visibility}
import io.deepsense.commons.service.db.CommonSlickFormats
import io.deepsense.seahorse.datasource.model.CsvSeparatorType.{apply => _, _}
import io.deepsense.seahorse.datasource.model.Visibility.{apply => _, _}

object DatasourcesSchema {

  import Database.api._
  import CommonSlickFormats._

  case class DatasourceDB(
    id: UUID,
    ownerId: UUID,
    ownerName: String,
    name: String,
    creationDateTime: java.util.Date,
    visibility: Visibility,
    downloadUri: Option[String],
    datasourceType: DatasourceType,
    jdbcUrl: Option[String],
    jdbcDriver: Option[String],
    jdbcTable: Option[String],
    jdbcQuery: Option[String],
    externalFileUrl: Option[String],
    hdfsPath: Option[String],
    libraryPath: Option[String],
    fileFormat: Option[FileFormat],
    fileCsvIncludeHeader: Option[Boolean],
    fileCsvConvert01ToBoolean: Option[Boolean],
    fileCsvSeparatorType: Option[CsvSeparatorType],
    fileCsvCustomSeparator: Option[String],
    googleSpreadsheetId: Option[String],
    googleServiceAccountCredentials: Option[String]
  )

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

    def * = (id, ownerId, ownerName, name, creationDateTime, visibility, downloadUri, datasourceType, jdbcUrl,
      jdbcDriver, jdbcTable, jdbcQuery, externalFileUrl, hdfsPath, libraryPath, fileFormat, fileCsvIncludeHeader,
      fileCsvConvert01ToBoolean, fileCsvSeparatorType, fileCsvCustomSeparator,
      googleSpreadsheetId, googleServiceAccountCredentials
      ) <> (DatasourceDB.tupled, DatasourceDB.unapply)
  }

  lazy val datasourcesTable = TableQuery[DatasourceTable]
}

// sbt-native-package won't work with multiple Mains
// https://github.com/sbt/sbt-native-packager/pull/319
// TODO use sbt-docker with sbt-assembly and define mainClass in assembly as
// it's solved in Neptune

/*
object PrintDDL extends App {
  import Database.api._
  import DatasourcesSchema._
  // scalastyle:off println
  println(datasourcesTable.schema.createStatements.mkString("\n"))
  // scalastyle:on println
}
*/
