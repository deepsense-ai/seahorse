/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db.dbio

import java.util.UUID

import slick.dbio.Effect.{Read, Write}

import io.deepsense.seahorse.datasource.api.ApiExceptions
import io.deepsense.seahorse.datasource.converters.DatasourceConverters
import io.deepsense.seahorse.datasource.db.Database
import io.deepsense.seahorse.datasource.db.schema._
import io.deepsense.seahorse.datasource.model.Datasource

object DatasourcesDBIOs {

  import scala.concurrent.ExecutionContext.Implicits.global

  import Database.api._
  import DatasourcesSchema._

  def getAll: DBIOAction[List[Datasource], NoStream, Read] = for {
    datasourceDbs <- datasourcesTable.result
  } yield datasourceDbs.map(DatasourceConverters.fromDb).toList

  def insertOrUpdate(id: UUID, datasource: Datasource): DBIOAction[Datasource, NoStream, Write with Read] = for {
    _ <- pathParamsMustMatchBodyParams(id, datasource)
    insertedCount <- datasourcesTable.insertOrUpdate(DatasourceConverters.fromApi(id, datasource))
    justInserted <- datasourcesTable.filter(_.id === id).result.head
  } yield DatasourceConverters.fromDb(justInserted)

  def delete(id: UUID): DBIOAction[Unit, NoStream, Write] = for {
    _ <- datasourcesTable.filter(_.id === id).delete
  } yield ()

  private def pathParamsMustMatchBodyParams(id: UUID, datasource: Datasource): DBIOAction[Unit, NoStream, Effect] = {
    if (datasource.id == id) {
      DBIO.successful(())
    } else {
      DBIO.failed(ApiExceptions.PathIdMustMatchBodyId(id, datasource.id))
    }
  }

  // Possible validation:
  // - filePath starts with declared fileScheme - reuse code from deeplang
  // - jdbcParams/fileParams none for other datasourceTypes
  // - csvParams none for other fileformats

}
