/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.seahorse.datasource.db.dbio

import java.util.UUID

import scala.util.{Failure, Success}

import ai.deepsense.commons.service.api.CommonApiExceptions
import ai.deepsense.commons.utils.LoggerForCallerClass
import ai.deepsense.seahorse.datasource.converters.{DatasourceApiFromDb, DatasourceDbFromApi}
import ai.deepsense.seahorse.datasource.db.Database
import ai.deepsense.seahorse.datasource.db.schema.DatasourcesSchema
import ai.deepsense.seahorse.datasource.model.{Datasource, DatasourceParams}

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
