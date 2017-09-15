/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperations

import org.scalatest._

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.inout._

class GoogleSheetInReadDataFrameSpec
  extends FreeSpec with BeforeAndAfterAll with LocalExecutionContext with Matchers with Logging {

  "Google sheet can be used as input" in {
    // TODO Once write df is supported use it to upload google sheet first
    val dataframe = read("1vI74zw07kIIfmvmciEbWkwDnGG_DI66ErBYaE-o02MY")
    dataframe.schema.get.fields.length should be > 1
  }

  private def read(googleSheetId: String): DataFrame = {
    val readDF = new ReadDataFrame()
      .setStorageType(
        new InputStorageTypeChoice.GoogleSheet()
          .setGoogleSheetId(googleSheetId)
      )
    readDF.execute(executionContext)(Vector.empty[DOperable]).head.asInstanceOf[DataFrame]
  }

}
