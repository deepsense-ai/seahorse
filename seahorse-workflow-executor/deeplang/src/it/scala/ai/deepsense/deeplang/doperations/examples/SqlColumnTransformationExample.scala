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

package ai.deepsense.deeplang.doperations.examples

import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import ai.deepsense.deeplang.doperations.SqlColumnTransformation
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class SqlColumnTransformationExample extends AbstractOperationExample[SqlColumnTransformation] {

  override def dOperation: SqlColumnTransformation = {
    val o = new SqlColumnTransformation()
    val myalias: String = "myAlias"

    val inPlace = NoInPlaceChoice()
      .setOutputColumn("WeightCutoff")
    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("Weight"))
      .setInPlace(inPlace)
    o.transformer.setFormula("MINIMUM(" + myalias + ", 2.0)")
      .setInputColumnAlias(myalias)
      .setSingleOrMultiChoice(single)
    o.set(o.transformer.extractParamMap())
  }

  override def fileNames: Seq[String] = Seq("example_animals")
}
