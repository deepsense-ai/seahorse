/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db.dbio

import java.util.UUID

import scala.util.{Failure, Success}

import io.deepsense.commons.service.api.CommonApiExceptions
import io.deepsense.commons.utils.LoggerForCallerClass
import io.deepsense.seahorse.datasource.converters.{DatasourceApiFromDb, DatasourceDbFromApi}
import io.deepsense.seahorse.datasource.db.Database
import io.deepsense.seahorse.datasource.db.schema.DatasourcesSchema
import io.deepsense.seahorse.datasource.model.{Datasource, DatasourceParams}

object InsertOrUpdate {

  import scala.concurrent.ExecutionContext.Implicits.global

  import Database.api._
  import DatasourcesSchema._

  val logger = LoggerForCallerClass()

  def apply(
      ownerId: UUID,
      ownerName: String,
      datasourceId: UUID,
      datasourceParams: DatasourceParams): DBIO[Datasource] =
    for {
      datasource <- DatasourceDbFromApi(ownerId, ownerName, datasourceId, datasourceParams).asDBIO
      updatedCount <- datasourcesTable.filter(ds =>
        ds.id === datasourceId && ds.ownerId === ownerId
      ).update(datasource)
      notExistsOrOwnedByOtherUser = updatedCount == 0
      _ <- if (notExistsOrOwnedByOtherUser) {
        val insert = datasourcesTable += datasource
        val insertAsTryInCaseOfPrimaryKeyCollision = insert.asTry
        insertAsTryInCaseOfPrimaryKeyCollision.flatMap {
          case Success(1) => successfullInsertBecauseItDidntExist
          case Failure(ex) => // TODO PK Collision is expected. Other exception should be unexpected
            logger.warn("Illegal edit of other users resource", ex)
            failedBecauseAlreadyExistedForOtherUser
        }
      } else {
        DBIO.successful(())
      }
      justUpserted <- datasourcesTable.filter(_.id === datasourceId).result.head
      apiDatasource <- DatasourceApiFromDb(ownerId, justUpserted).asDBIO
    } yield apiDatasource

  private val successfullInsertBecauseItDidntExist = DBIO.successful(())
  private val failedBecauseAlreadyExistedForOtherUser = DBIO.failed(CommonApiExceptions.forbidden)

}
