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

import ai.deepsense.deeplang.utils.DataFrameMatchers
import ai.deepsense.deeplang.{InMemoryDataFrame, LocalExecutionContext, TestDataSources, TestFiles}

class WriteReadDatasourceIntegSpec
  extends FreeSpec with BeforeAndAfter with BeforeAndAfterAll
    with LocalExecutionContext with InMemoryDataFrame with TestFiles with TestDataSources {

  for (ds <- someDatasourcesForWriting) {
    s"`${ds.getParams.getName}` datasource should be readable and writeable" in {
      val wds = WriteDatasource().setDatasourceId(ds.getId)
      wds.execute(inMemoryDataFrame)(context)

      val rds = ReadDatasource().setDatasourceId(ds.getId)
      val dataframe = rds.execute()(context)

      DataFrameMatchers.assertDataFramesEqual(dataframe, inMemoryDataFrame)
    }
  }

  private val context = LocalExecutionContext.createExecutionContext(datasourceClient)

}
