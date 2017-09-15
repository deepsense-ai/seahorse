/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db.dbio

import java.util.UUID

import io.deepsense.commons.service.api.CommonApiExceptions
import io.deepsense.commons.service.db.dbio.GenericDBIOs
import io.deepsense.seahorse.datasource.converters.DatasourceApiFromDb
import io.deepsense.seahorse.datasource.db.Database
import io.deepsense.seahorse.datasource.db.schema.DatasourcesSchema
import io.deepsense.seahorse.datasource.model.{Datasource, Visibility}

object Get {

  import scala.concurrent.ExecutionContext.Implicits.global

  import Database.api._
  import DatasourcesSchema._

  def apply(callingUserId: UUID, datasourceId: UUID): DBIO[Datasource] = for {
    datasourceOpt <- datasourcesTable.filter(_.id === datasourceId).result.headOption
    datasource <- GenericDBIOs.checkExists(datasourceId, datasourceOpt)
    _ <- checkIfForbidden(datasource, callingUserId)
    apiDatasource <- DatasourceApiFromDb(callingUserId, datasource).asDBIO
  } yield apiDatasource

  private def checkIfForbidden(ds: DatasourceDB, callingUserId: UUID) =
    if (ds.visibility == Visibility.publicVisibility || ds.ownerId == callingUserId) {
      DBIO.successful(())
    } else {
      DBIO.failed(CommonApiExceptions.forbidden)
    }

}
