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

package ai.deepsense.workflowexecutor.session.storage

import org.apache.spark.sql.{DataFrame => SparkDataFrame}
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar

import ai.deepsense.commons.StandardSpec
import ai.deepsense.commons.models.Id
import ai.deepsense.deeplang.DataFrameStorage
import ai.deepsense.deeplang.doperables.dataframe.DataFrame

class DataFrameStorageSpec
    extends StandardSpec
    with BeforeAndAfter
    with MockitoSugar {

  val workflow1Id = Id.randomId
  val workflow2Id = Id.randomId

  val node1Id = Id.randomId
  val node2Id = Id.randomId

  val dataframe1Id = "dataframe1"
  val dataframe2Id = "dataframe2"
  val dataframe3Id = "dataframe3"

  val dataframe1 = mock[DataFrame]
  val dataframe2 = mock[DataFrame]
  val dataframe3 = mock[DataFrame]

  val sparkDataFrame1 = mock[SparkDataFrame]
  val sparkDataFrame2 = mock[SparkDataFrame]
  val sparkDataFrame3 = mock[SparkDataFrame]

  var storage: DataFrameStorage = _

  before {
    storage = new DataFrameStorageImpl
  }

  "DataFrameStorage" should {
    "register input dataFrames" in {
      storage.setInputDataFrame(workflow1Id, node1Id, 0, sparkDataFrame1)
      storage.setInputDataFrame(workflow1Id, node2Id, 0, sparkDataFrame2)
      storage.setInputDataFrame(workflow1Id, node2Id, 1, sparkDataFrame3)

      storage.getInputDataFrame(workflow1Id, node1Id, 0) shouldBe Some(sparkDataFrame1)
      storage.getInputDataFrame(workflow1Id, node1Id, 1) shouldBe None
      storage.getInputDataFrame(workflow1Id, node2Id, 0) shouldBe Some(sparkDataFrame2)
      storage.getInputDataFrame(workflow1Id, node2Id, 1) shouldBe Some(sparkDataFrame3)
      storage.getInputDataFrame(workflow2Id, node2Id, 2) shouldBe None
    }

    "delete input dataFrames" in {
      storage.setInputDataFrame(workflow1Id, node1Id, 0, sparkDataFrame1)
      storage.setInputDataFrame(workflow1Id, node2Id, 0, sparkDataFrame2)
      storage.setInputDataFrame(workflow1Id, node2Id, 1, sparkDataFrame3)
      storage.setInputDataFrame(workflow2Id, node2Id, 0, sparkDataFrame3)

      storage.removeNodeInputDataFrames(workflow1Id, node2Id, 0)

      storage.getInputDataFrame(workflow1Id, node1Id, 0) shouldBe Some(sparkDataFrame1)
      storage.getInputDataFrame(workflow1Id, node1Id, 1) shouldBe None
      storage.getInputDataFrame(workflow1Id, node2Id, 0) shouldBe None
      storage.getInputDataFrame(workflow1Id, node2Id, 1) shouldBe Some(sparkDataFrame3)
      storage.getInputDataFrame(workflow2Id, node2Id, 2) shouldBe None
      storage.getInputDataFrame(workflow2Id, node2Id, 0) shouldBe Some(sparkDataFrame3)
    }

    "register output dataFrames" in {
      storage.setOutputDataFrame(workflow1Id, node1Id, 0, sparkDataFrame1)
      storage.setOutputDataFrame(workflow1Id, node2Id, 0, sparkDataFrame2)
      storage.setOutputDataFrame(workflow1Id, node2Id, 1, sparkDataFrame3)

      storage.getOutputDataFrame(workflow1Id, node1Id, 0) shouldBe Some(sparkDataFrame1)
      storage.getOutputDataFrame(workflow1Id, node1Id, 1) shouldBe None
      storage.getOutputDataFrame(workflow1Id, node2Id, 0) shouldBe Some(sparkDataFrame2)
      storage.getOutputDataFrame(workflow1Id, node2Id, 1) shouldBe Some(sparkDataFrame3)
      storage.getOutputDataFrame(workflow2Id, node2Id, 2) shouldBe None
    }

    "delete some output dataFrames" in {
      storage.setOutputDataFrame(workflow1Id, node1Id, 0, sparkDataFrame1)
      storage.setOutputDataFrame(workflow1Id, node2Id, 0, sparkDataFrame2)
      storage.setOutputDataFrame(workflow1Id, node2Id, 1, sparkDataFrame3)
      storage.setOutputDataFrame(workflow2Id, node2Id, 1, sparkDataFrame3)

      storage.removeNodeOutputDataFrames(workflow1Id, node2Id)

      storage.getOutputDataFrame(workflow1Id, node1Id, 0) shouldBe Some(sparkDataFrame1)
      storage.getOutputDataFrame(workflow1Id, node2Id, 0) shouldBe None
      storage.getOutputDataFrame(workflow1Id, node2Id, 1) shouldBe None
      storage.getOutputDataFrame(workflow2Id, node2Id, 1) shouldBe Some(sparkDataFrame3)
    }
  }
}
