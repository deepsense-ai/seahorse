/**
  * Copyright 2018 deepsense.ai (CodiLime, Inc)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package ai.deepsense.seahorse.datasource.converters

import java.util.UUID

import ai.deepsense.seahorse.datasource.db.schema.DatasourcesSchema.SparkOptionDB
import ai.deepsense.seahorse.datasource.model.SparkGenericOptions

object SparkOptionDbFromApi {

    def apply(sparkOption: SparkGenericOptions, datasourceId: UUID): SparkOptionDB = {
      SparkOptionDB(UUID.randomUUID(), sparkOption.key, sparkOption.value, datasourceId)
    }

    def apply(sparkOptions: List[SparkGenericOptions], datasourceId: UUID): List[SparkOptionDB] = {
      sparkOptions.map(apply(_, datasourceId))
    }
}
