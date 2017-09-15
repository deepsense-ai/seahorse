/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperables

import scala.concurrent.Await

import io.deepsense.entitystorage.EntityStorageClient
import io.deepsense.models.entities.Entity

trait DOperableLoader {

  def load[T](
      entityStorageClient: EntityStorageClient)(
      loader: (String => T))(
      tenantId: String,
      id: Entity.Id): T = {
    import scala.concurrent.duration._
    // TODO: duration from configuration (and possibly a little longer timeout)
    implicit val timeout = 5.seconds
    val entityF = entityStorageClient.getEntityData(tenantId, id)
    val entity = Await.result(entityF, timeout).get
    loader(entity.dataReference.savedDataPath)
  }
}

object DOperableLoader extends DOperableLoader
