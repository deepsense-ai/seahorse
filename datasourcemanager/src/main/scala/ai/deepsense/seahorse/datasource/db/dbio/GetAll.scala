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

import ai.deepsense.seahorse.datasource.converters.DatasourceApiFromDb
import ai.deepsense.seahorse.datasource.db.Database
import ai.deepsense.seahorse.datasource.db.schema.DatasourcesSchema
import ai.deepsense.seahorse.datasource.model.{Datasource, Visibility}

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
