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

package io.deepsense.deeplang.doperations.examples

import io.deepsense.deeplang.doperations.ExecuteMathematicalTransformation
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection

class ExecuteMathematicalTransformationExample
  extends AbstractOperationExample[ExecuteMathematicalTransformation] {

  override def dOperation: ExecuteMathematicalTransformation = {
    val o = new ExecuteMathematicalTransformation()
    val myalias: String = "myAlias"
    o.transformer.setFormula("MINIMUM(" + myalias + ", 2.0)")
      .setInputColumn(NameSingleColumnSelection("Weight"))
      .setInputColumnAlias(myalias)
      .setOutputColumnName("WeightCutoff")
    o.set(o.transformer.extractParamMap())
  }

  override def fileNames: Seq[String] = Seq("example_animals")
}
