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

package ai.deepsense.deeplang.doperables.spark.wrappers.transformers

import org.apache.spark.ml.feature.{RegexTokenizer => SparkRegexTokenizer}

import ai.deepsense.deeplang.doperables.SparkTransformerAsMultiColumnTransformer
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.{BooleanParamWrapper, IntParamWrapper, StringParamWrapper}

class RegexTokenizer extends SparkTransformerAsMultiColumnTransformer[SparkRegexTokenizer] {

  val gaps = new BooleanParamWrapper[SparkRegexTokenizer](
    name = "gaps",
    description = Some("Indicates whether the regex splits on gaps (true) or matches tokens (false)."),
    sparkParamGetter = _.gaps)
  setDefault(gaps, true)

  val minTokenLength = new IntParamWrapper[SparkRegexTokenizer](
    name = "min token length",
    description = Some("The minimum token length."),
    sparkParamGetter = _.minTokenLength,
    validator = RangeValidator.positiveIntegers)
  setDefault(minTokenLength, 1.0)

  val pattern = new StringParamWrapper[SparkRegexTokenizer](
    name = "pattern",
    description =
      Some("""The regex pattern used to match delimiters (gaps = true) or tokens
        |(gaps = false).""".stripMargin),
    sparkParamGetter = _.pattern)
  setDefault(pattern, "\\s+")

  override protected def getSpecificParams: Array[Param[_]] = Array(gaps, minTokenLength, pattern)
}
