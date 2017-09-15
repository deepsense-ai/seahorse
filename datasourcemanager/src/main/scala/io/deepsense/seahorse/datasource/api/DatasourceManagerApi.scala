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
import io.deepsense.seahorse._
import io.deepsense.seahorse.datasource.DatasourceManagerConfig
import io.deepsense.seahorse.datasource.db.Database
import io.deepsense.seahorse.datasource.model._

class DatasourceManagerApi extends DefaultApi {

  override def putDatasourceImpl(
      userId: UUID,
      userName: String,
      datasourceId: UUID,
      datasourceParams: DatasourceParams) =
    datasource.db.dbio.InsertOrUpdate(userId, userName, datasourceId, datasourceParams).run()

  override def getDatasourceImpl(userId: UUID, datasourceId: UUID) =
    datasource.db.dbio.Get(userId, datasourceId).run()

  override def getDatasourcesImpl(callingUserId: UUID) =
    datasource.db.dbio.GetAll(callingUserId).run()

  override def deleteDatasourceImpl(callingUserId: UUID, datasourceId: UUID) =
    datasource.db.dbio.Delete(callingUserId, datasourceId).run()

  // Codegen abstracts from application-specific error body format
  override protected def formatErrorBody(code: Int, msg: String): String = JsonBodyForError(code, msg)

  // TODO DRY with Scheduling Manager
  implicit class DBIOOps[T](dbio: DBIO[T]) {
    import scala.concurrent.duration._
    def run(): T = {
      val futureResult = Database.db.run(dbio.withPinnedSession)
      Try {
        Await.result(futureResult, DatasourceManagerConfig.database.timeout)
      } match {
        case Success(value) => value
        case Failure(commonEx: CommonApiExceptions.ApiException) => throw ApiExceptionFromCommon(commonEx)
        case Failure(other) => throw other
      }
    }
    implicit def durationJavaToScala(d: java.time.Duration): Duration = Duration.fromNanos(d.toNanos)
  }

}
