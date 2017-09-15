/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec}

import ai.deepsense.deeplang.{LocalExecutionContext, TestDataSources, TestFiles}

class ReadDatasourceIntegSpec
  extends FreeSpec
  with BeforeAndAfter
  with BeforeAndAfterAll
  with LocalExecutionContext
  with TestDataSources
  with TestFiles {

  for (ds <- someDatasourcesForReading) {
    s"ReadDatasource should work with datasource ${ds.getParams.getName}" in {
      val rds = ReadDatasource().setDatasourceId(ds.getId)
      rds.execute()(LocalExecutionContext.createExecutionContext(datasourceClient))
    }
  }

}
