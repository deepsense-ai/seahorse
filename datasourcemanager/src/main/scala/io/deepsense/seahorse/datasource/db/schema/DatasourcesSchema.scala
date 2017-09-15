/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db.schema

import java.sql.JDBCType
import java.util.UUID

import slick.jdbc.{PositionedParameters, SetParameter}

import io.deepsense.seahorse.datasource.Configs
import io.deepsense.seahorse.datasource.db.{Database, EnumColumnMapper}
import io.deepsense.seahorse.datasource.model.DatasourceType.{apply => _, _}
import io.deepsense.seahorse.datasource.model.FileFormat._
import io.deepsense.seahorse.datasource.model.FileScheme.{apply => _, _}
import io.deepsense.seahorse.datasource.model.{DatasourceType, FileFormat, FileScheme}

object DatasourcesSchema {

  import Database.api._

  case class DatasourceDB(
    id: UUID,
    name: String,
    downloadUri: Option[String],
    datasourceType: DatasourceType,
    jdbcUrl: Option[String],
    jdbcDriver: Option[String],
    jdbcTable: Option[String],
    filePath: Option[String],
    fileScheme: Option[FileScheme],
    fileFormat: Option[FileFormat],
    fileCsvSeparator: Option[String],
    fileCsvIncludeHeader: Option[Boolean]
  )

  implicit val uuidFormat = new SetParameter[UUID] {
    def apply(v: UUID, pp: PositionedParameters): Unit = {
      pp.setObject(v, JDBCType.BINARY.getVendorTypeNumber)
    }
  }

  implicit val fileSchemeFormat = EnumColumnMapper(FileScheme)
  implicit val datasourceTypeFormat = EnumColumnMapper(DatasourceType)
  implicit val fileFormatFormat = EnumColumnMapper(FileFormat)

  final class DatasourceTable(tag: Tag)
      extends Table[DatasourceDB](tag, Some(Configs.Database.schema), "datasource") {
    def id = column[UUID]("id", O.PrimaryKey)
    def name = column[String]("name")
    def downloadUri = column[Option[String]]("downloadUri")
    def datasourceType = column[DatasourceType]("datasourceType")
    def jdbcUrl = column[Option[String]]("jdbcUrl")
    def jdbcDriver = column[Option[String]]("jdbcDriver")
    def jdbcTable = column[Option[String]]("jdbcTable")
    def filePath = column[Option[String]]("filePath")
    def fileScheme = column[Option[FileScheme]]("fileScheme")
    def fileFormat = column[Option[FileFormat]]("fileFormat")
    def fileCsvSeparator = column[Option[String]]("fileCsvSeparator")
    def fileCsvIncludeHeader = column[Option[Boolean]]("fileCsvIncludeHeader")

    def * = (id, name, downloadUri, datasourceType, jdbcUrl, jdbcDriver, jdbcTable, filePath, fileScheme,
        fileFormat, fileCsvSeparator, fileCsvIncludeHeader
      ) <> (DatasourceDB.tupled, DatasourceDB.unapply _)
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
