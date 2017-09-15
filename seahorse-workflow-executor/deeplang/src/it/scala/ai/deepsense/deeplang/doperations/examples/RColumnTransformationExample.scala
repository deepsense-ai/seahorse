/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations.examples

import ai.deepsense.deeplang.doperables.{RColumnTransformer, TargetTypeChoices}
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import ai.deepsense.deeplang.doperations.RColumnTransformation
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection
import ai.deepsense.deeplang.{DOperable, ExecutionContext}

class RColumnTransformationExample
  extends AbstractOperationExample[RColumnTransformation] {

  val poundInKg = 0.45359237
  val inputColumnName = "Weight"
  val outputColumnName = "WeightInPounds"

  // This is mocked because R executor is not available in tests.
  class RColumnTransformationMock extends RColumnTransformation {
    override def execute(arg: DataFrame)(context: ExecutionContext): (DataFrame, RColumnTransformer) = {
      val sdf = arg.sparkDataFrame
      val resultSparkDataFrame = sdf.select(
        sdf("*"),
        (sdf(inputColumnName) / poundInKg).alias(outputColumnName))
      (DataFrame.fromSparkDataFrame(resultSparkDataFrame), mock[RColumnTransformer])
    }
  }

  override def dOperation: RColumnTransformation = {
    val o = new RColumnTransformationMock()

    val inPlace = NoInPlaceChoice()
      .setOutputColumn(s"$outputColumnName")
    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection(inputColumnName))
      .setInPlace(inPlace)
    o.transformer
      .setTargetType(TargetTypeChoices.DoubleTargetTypeChoice())
      .setSingleOrMultiChoice(single)
      .setCodeParameter(
        "transform.column <- function(column, column.name) {" +
          s"\n  return(column / $poundInKg)" +
          "\n}")
    o.set(o.transformer.extractParamMap())
  }

  override def fileNames: Seq[String] = Seq("example_animals")
}
