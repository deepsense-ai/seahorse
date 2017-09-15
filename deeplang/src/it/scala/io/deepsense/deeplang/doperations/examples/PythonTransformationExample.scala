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

import org.apache.spark.sql.functions._

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.deeplang.doperations.PythonTransformation

class PythonTransformationExample extends AbstractOperationExample[PythonTransformation] {

  class PythonTransformationMock extends PythonTransformation {
    override def execute(context: ExecutionContext)
                        (arguments: Vector[DOperable]): Vector[DOperable] = {
      PythonTransformationExample.execute(context)(arguments)
    }
  }

  override def dOperation: PythonTransformation = {
    val op = new PythonTransformationMock()
    op.transformer
      .setCodeParameter(
        "def transform(df):" +
          "\n    return df.filter(df.temp > 0.4).sort(df.windspeed, ascending=False)")
    op.set(op.transformer.extractParamMap())

  }

  override def fileNames: Seq[String] = Seq("example_datetime_windspeed_hum_temp")
}

object PythonTransformationExample {
  def execute(context: ExecutionContext)(arguments: Vector[DOperable]): Vector[DOperable] = {
    val sdf = arguments.head.asInstanceOf[DataFrame].sparkDataFrame
    val resultSparkDataFrame = sdf.filter("temp > 0.4").sort(desc("windspeed"))
    Vector(DataFrame.fromSparkDataFrame(resultSparkDataFrame))
  }
}
