/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db.dbio

import java.util.UUID

import io.deepsense.seahorse.datasource.converters.DatasourceApiFromDb
import io.deepsense.seahorse.datasource.db.Database
import io.deepsense.seahorse.datasource.db.schema.DatasourcesSchema
import io.deepsense.seahorse.datasource.model.{Datasource, Visibility}

object GetAll {

  import scala.concurrent.ExecutionContext.Implicits.global

  import Database.api._
  import DatasourcesSchema._

  def apply(callingUserId: UUID): DBIO[List[Datasource]] = for {
    datasources <- datasourcesTable.filter(ds => visibleByUser(ds, callingUserId)).result
    apiDatasources <- DBIO.sequence(datasources.map(DatasourceApiFromDb(callingUserId, _).asDBIO))
  } yield apiDatasources.toList

  private def visibleByUser(ds: DatasourceTable, callingUserId: UUID) = {
    ds.visibility === Visibility.publicVisibility || ds.ownerId === callingUserId
  }

}
