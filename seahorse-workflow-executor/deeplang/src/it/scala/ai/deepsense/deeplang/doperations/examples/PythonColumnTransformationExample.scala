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

import org.apache.spark.sql.functions.when

import ai.deepsense.deeplang.{DOperable, ExecutionContext}
import ai.deepsense.deeplang.doperables.{PythonColumnTransformer, TargetTypeChoices}
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import ai.deepsense.deeplang.doperations.PythonColumnTransformation
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class PythonColumnTransformationExample
  extends AbstractOperationExample[PythonColumnTransformation] {

  val inputColumnName = "Weight"
  val outputColumnName = "WeightCutoff"

  // This is mocked because Python executor is not available in tests.
  class PythonColumnTransformationMock extends PythonColumnTransformation {
    override def execute(arg: DataFrame)(context: ExecutionContext): (DataFrame, PythonColumnTransformer) = {
      val sdf = arg.sparkDataFrame
      val resultSparkDataFrame = sdf.select(
        sdf("*"),
        when(sdf(inputColumnName) > 2.0, 2.0).otherwise(sdf(inputColumnName))
          .alias(outputColumnName))
      (DataFrame.fromSparkDataFrame(resultSparkDataFrame), mock[PythonColumnTransformer])
    }
  }

  override def dOperation: PythonColumnTransformation = {
    val op = new PythonColumnTransformationMock()

    val inPlace = NoInPlaceChoice()
      .setOutputColumn(s"$outputColumnName")
    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection(inputColumnName))
      .setInPlace(inPlace)
    op.transformer
      .setTargetType(TargetTypeChoices.DoubleTargetTypeChoice())
      .setSingleOrMultiChoice(single)
      .setCodeParameter(
        "def transform_value(value, column_name):\n" +
          "    return min(value, 2.0)")
    op.set(op.transformer.extractParamMap())
  }

  override def fileNames: Seq[String] = Seq("example_animals")
}
