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

package io.deepsense.deeplang.doperations.examples


import io.deepsense.deeplang.doperations.{RTransformation}
import io.deepsense.deeplang.{DOperable, ExecutionContext}

class RTransformationExample
  extends AbstractOperationExample[RTransformation] {

  class RTransformationMock extends RTransformation {
    override def execute(context: ExecutionContext)
                        (arguments: Vector[DOperable]): Vector[DOperable] = {
      PythonTransformationExample.execute(context)(arguments)
    }
  }

  override def dOperation: RTransformation = {
    val op = new RTransformationMock()
    op.transformer
      .setCodeParameter(
        "transform <- function(dataframe) {" +
          "\n  filtered_df <- filter(dataframe, dataframe$temp > 0.4)" +
          "\n  sorted_filtered_df <- orderBy(filtered_df, desc(filtered_df$windspeed))" +
          "\n  return(sorted_filtered_df)" +
          "\n}")
    op.set(op.transformer.extractParamMap())

  }

  override def fileNames: Seq[String] = Seq("example_datetime_windspeed_hum_temp")
}
