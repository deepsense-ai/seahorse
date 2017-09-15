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

package io.deepsense.deeplang

import org.apache.spark.sql.{DataFrame => SparkDataFrame}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import io.deepsense.commons.models.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame

class ContextualDataFrameStorageSpec
    extends UnitSpec
    with BeforeAndAfter {

  val workflowId = Id.randomId
  val nodeId = Id.randomId
  val portNumber = 332

  val dataFrame = mock[DataFrame]
  val sparkDataFrame = mock[SparkDataFrame]

  val dataFrameStorage = mock[DataFrameStorage]

  var storage: ContextualDataFrameStorage = _

  before {
    storage = new ContextualDataFrameStorage(dataFrameStorage, workflowId, nodeId)
  }

  "ContextualDataFrameStorage" should {
    "store input dataFrame" in {
      storage.setInputDataFrame(portNumber, sparkDataFrame)

      verify(dataFrameStorage).setInputDataFrame(workflowId, nodeId, portNumber, sparkDataFrame)
    }

    "store output dataFrame" in {
      storage.setOutputDataFrame(portNumber, sparkDataFrame)

      verify(dataFrameStorage).setOutputDataFrame(workflowId, nodeId, portNumber, sparkDataFrame)
    }

    "get output dataFrame" in {
      when(dataFrameStorage.getOutputDataFrame(workflowId, nodeId, portNumber))
        .thenReturn(Some(sparkDataFrame))

      storage.getOutputDataFrame(portNumber) shouldBe Some(sparkDataFrame)
    }
  }
}
