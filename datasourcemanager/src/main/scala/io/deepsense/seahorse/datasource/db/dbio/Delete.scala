/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db.dbio

import java.util.UUID

import io.deepsense.commons.service.api.CommonApiExceptions
import io.deepsense.seahorse.datasource.db.Database
import io.deepsense.seahorse.datasource.db.schema.DatasourcesSchema

object Delete {

  import scala.concurrent.ExecutionContext.Implicits.global

  import Database.api._
  import DatasourcesSchema._

  def apply(callingUserId: UUID, datasourceId: UUID): DBIO[Unit] = for {
    deletedCount <- datasourcesTable.filter(ds => ds.id === datasourceId && ds.ownerId === callingUserId).delete
    _ <- if (deletedCount == 0) {
      checkIfDeleteReturnedZeroBecauseOfForbiddenAccess(callingUserId, datasourceId)
    } else {
      DBIO.successful(())
    }
  } yield ()

  private def checkIfDeleteReturnedZeroBecauseOfForbiddenAccess(callingUserId: UUID, datasourceId: UUID) = for {
    datasourceOpt <- datasourcesTable.filter(_.id === datasourceId).result.headOption
    _ <- datasourceOpt match {
      case Some(_) => DBIO.failed(CommonApiExceptions.forbidden)
      case None => DBIO.failed(CommonApiExceptions.doesNotExist(datasourceId))
    }
  } yield ()

}
