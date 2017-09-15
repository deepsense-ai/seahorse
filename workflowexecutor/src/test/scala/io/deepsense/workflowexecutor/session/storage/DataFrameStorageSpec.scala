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

package io.deepsense.workflowexecutor.session.storage

import org.apache.spark.sql.{DataFrame => SparkDataFrame}
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.models.Id
import io.deepsense.deeplang.DataFrameStorage
import io.deepsense.deeplang.doperables.dataframe.DataFrame

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

  var storage: DataFrameStorage = _

  before {
    storage = new DataFrameStorageImpl
  }

  "DataFrameStorage" should {

    "register dataframe" in {
      storage.put(workflow1Id, dataframe1Id, dataframe1)
      storage.get(workflow1Id, dataframe1Id) shouldBe Some(dataframe1)
    }

    "list dataframes" in {
      storage.put(workflow1Id, dataframe1Id, dataframe1)
      storage.put(workflow1Id, dataframe2Id, dataframe2)
      storage.put(workflow2Id, dataframe3Id, dataframe3)

      val storedIds = storage.listDataFrameNames(workflow1Id)
      storedIds should contain theSameElementsAs Seq(dataframe1Id, dataframe2Id)
    }

    "return None" when {

      "dataframe was not registered" in {
        storage.get(workflow1Id, dataframe1Id) shouldBe None
      }

      "different workflow id was passed" in {
        storage.put(workflow1Id, dataframe1Id, dataframe1)
        storage.get(workflow2Id, dataframe1Id) shouldBe None
      }

      "different dataframe id was passed" in {
        storage.put(workflow1Id, dataframe1Id, dataframe1)
        storage.get(workflow1Id, dataframe2Id) shouldBe None
      }
    }

    "register input dataframes" in {
      storage.setInputDataFrame(workflow1Id, node1Id, sparkDataFrame1)
      storage.setInputDataFrame(workflow1Id, node2Id, sparkDataFrame2)

      storage.getInputDataFrame(workflow1Id, node1Id) shouldBe Some(sparkDataFrame1)
      storage.getInputDataFrame(workflow1Id, node2Id) shouldBe Some(sparkDataFrame2)
      storage.getInputDataFrame(workflow2Id, node1Id) shouldBe None
    }

    "register output dataframes" in {
      storage.setOutputDataFrame(workflow1Id, node1Id, sparkDataFrame1)
      storage.setOutputDataFrame(workflow1Id, node2Id, sparkDataFrame2)

      storage.getOutputDataFrame(workflow1Id, node1Id) shouldBe Some(sparkDataFrame1)
      storage.getOutputDataFrame(workflow1Id, node2Id) shouldBe Some(sparkDataFrame2)
      storage.getOutputDataFrame(workflow2Id, node1Id) shouldBe None
    }
  }
}
