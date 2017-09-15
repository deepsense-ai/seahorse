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
import ai.deepsense.commons.service.db.dbio.GenericDBIOs
import ai.deepsense.seahorse.datasource.DatasourceManagerConfig
import ai.deepsense.seahorse.datasource.converters.DatasourceApiFromDb
import ai.deepsense.seahorse.datasource.db.Database
import ai.deepsense.seahorse.datasource.db.schema.DatasourcesSchema
import ai.deepsense.seahorse.datasource.model.{Datasource, Visibility}

object Get {

  import scala.concurrent.ExecutionContext.Implicits.global

  import Database.api._
  import DatasourcesSchema._

  private val config = DatasourceManagerConfig.config

  val privilegedUsers: Set[UUID] = Set(DatasourceManagerConfig.schedulerUserConfigPath).map { userPath =>
    UUID.fromString(config.getString(
      s"${DatasourceManagerConfig.predefinedUsersConfigPath}.$userPath.id"))
  }

  def apply(callingUserId: UUID, datasourceId: UUID): DBIO[Datasource] = for {
    datasourceOpt <- datasourcesTable.filter(_.id === datasourceId).result.headOption
    datasource <- GenericDBIOs.checkExists(datasourceId, datasourceOpt)
    _ <- checkIfForbidden(datasource, callingUserId)
    apiDatasource <- DatasourceApiFromDb(callingUserId, datasource).asDBIO
  } yield apiDatasource

  private def checkIfForbidden(ds: DatasourceDB, callingUserId: UUID) =
    if (
      privilegedUsers.contains(callingUserId) ||
        ds.generalParameters.visibility == Visibility.publicVisibility ||
        ds.generalParameters.ownerId == callingUserId) {
      DBIO.successful(())
    } else {
      DBIO.failed(CommonApiExceptions.forbidden)
    }

}
