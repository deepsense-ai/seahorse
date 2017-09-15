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

package ai.deepsense.deeplang.doperables.spark.wrappers.params.common

import scala.language.reflectiveCalls

import org.apache.spark.ml

import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.DoubleParamWrapper

trait HasMinTermsFrequencyParam
  extends HasInputColumn
  with HasOutputColumn {

  val minTF = new DoubleParamWrapper[ml.param.Params { val minTF: ml.param.DoubleParam }](
    name = "min term frequency",
    description =
      Some("""A filter to ignore rare words in a document. For each document, terms with
        |a frequency/count less than the given threshold are ignored. If this is an integer >= 1,
        |then this specifies a count (of times the term must appear in the document); if this is
        |a double in [0,1), then it specifies a fraction (out of the document's token count).
        |Note that the parameter is only used in transform of CountVectorizer model and does not
        |affect fitting.""".stripMargin),
    sparkParamGetter = _.minTF,
    RangeValidator(0.0, Double.MaxValue))
  setDefault(minTF, 1.0)

  def setMinTF(value: Double): this.type = {
    set(minTF, value)
  }
}
