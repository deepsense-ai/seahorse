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

import ai.deepsense.commons.service.api.CommonApiExceptions
import ai.deepsense.seahorse.datasource.db.Database
import ai.deepsense.seahorse.datasource.db.schema.DatasourcesSchema

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
