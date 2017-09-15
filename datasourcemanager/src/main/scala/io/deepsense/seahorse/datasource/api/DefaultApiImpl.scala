/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.api

import java.util.UUID

import scala.concurrent.Await
import scala.language.postfixOps

import org.scalatra.CorsSupport
import slick.dbio.DBIO

import io.deepsense.seahorse.datasource.Configs
import io.deepsense.seahorse.datasource.db.Database
import io.deepsense.seahorse.datasource.db.dbio.DatasourcesDBIOs
import io.deepsense.seahorse.datasource.model._

class DefaultApiImpl extends DefaultApi with CorsSupport {

  override def deleteDatasourceImpl(datasourceId: UUID): Unit =
    DatasourcesDBIOs.delete(datasourceId).run()

  override def putDatasourceImpl(datasourceId: UUID, datasource: Datasource) =
    DatasourcesDBIOs.insertOrUpdate(datasourceId, datasource).run()

  override def getDatasourcesImpl() = DatasourcesDBIOs.getAll.run()

  // Codegen abstracts from application-specific error body format
  override protected def formatErrorBody(code: Int, msg: String): String = JsonBodyForError(code, msg)

  implicit class DBIOOps[T](dbio: DBIO[T]) {

    import scala.concurrent.duration._

    def run(): T = {
      val futureResult = Database.db.run(dbio.withPinnedSession)
      Await.result(futureResult, Configs.Database.timeout)
    }

    implicit def durationJavaToScala(d: java.time.Duration): Duration = Duration.fromNanos(d.toNanos)

  }

}
