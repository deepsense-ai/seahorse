/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.api

import java.util.UUID

import scala.concurrent.Await
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

import slick.dbio.DBIO

import io.deepsense.commons.service.api.CommonApiExceptions
import io.deepsense.commons.service.db.dbio.GenericDBIOs
import io.deepsense.seahorse.datasource.DatasourceManagerConfig
import io.deepsense.seahorse.datasource.converters.DatasourceConverters
import io.deepsense.seahorse.datasource.db.Database
import io.deepsense.seahorse.datasource.db.schema.DatasourcesSchema
import io.deepsense.seahorse.datasource.db.schema.DatasourcesSchema.DatasourceDB
import io.deepsense.seahorse.datasource.model._

class DatasourceManagerApi extends DefaultApi {

  private val genericDBIOs = new GenericDBIOs[Datasource, DatasourceDB] {
    override val api = io.deepsense.seahorse.datasource.db.Database.api
    override val table = DatasourcesSchema.datasourcesTable
    override val fromDB = DatasourceConverters.fromDb _
    override val fromApi = DatasourceConverters.fromApi _
  }

  override def deleteDatasourceImpl(datasourceId: UUID): Unit =
    genericDBIOs.delete(datasourceId).run()

  // Possible validation:
  // - filePath starts with declared fileScheme - reuse code from deeplang
  // - jdbcParams/fileParams none for other datasourceTypes
  // - csvParams none for other fileformats

  override def putDatasourceImpl(datasourceId: UUID, datasource: Datasource) =
    genericDBIOs.insertOrUpdate(datasourceId, datasource).run()

  override def getDatasourceImpl(datasourceId: UUID): Datasource =
    genericDBIOs.get(datasourceId).run()

  override def getDatasourcesImpl() = genericDBIOs.getAll.run()

  // Codegen abstracts from application-specific error body format
  override protected def formatErrorBody(code: Int, msg: String): String = JsonBodyForError(code, msg)

  // TODO DRY
  implicit class DBIOOps[T](dbio: DBIO[T]) {
    import scala.concurrent.duration._
    def run(): T = {
      val futureResult = Database.db.run(dbio.withPinnedSession)
      Try {
        Await.result(futureResult, DatasourceManagerConfig.database.timeout)
      } match {
        case Success(value) => value
        case Failure(commonEx: CommonApiExceptions.ApiException) => throw ApiExceptionFromCommon(commonEx)
      }
    }
    implicit def durationJavaToScala(d: java.time.Duration): Duration = Duration.fromNanos(d.toNanos)
  }

}
