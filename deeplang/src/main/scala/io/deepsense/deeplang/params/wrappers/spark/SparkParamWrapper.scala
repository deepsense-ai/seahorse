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

package io.deepsense.deeplang.params.wrappers.spark

import org.apache.spark.ml

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.params.Param

/**
 * This trait should be mixed in by Seahorse parameters derived from Spark parameters.
 * Wrapped Spark parameters should be able to convert Seahorse parameter values to Spark
 * parameter values.
 *
 * @tparam T Type of wrapped Spark parameter value
 * @tparam U Type of Seahorse parameter value
 */
trait SparkParamWrapper[T, U] extends Param[U] {
  val sparkParam: ml.param.Param[T]

  /**
   * Convert Seahorse parameter value to wrapped Spark parameter value.
   * @param value Seahorse parameter value
   * @param df DataFrame
   * @return Spark parameter value
   */
  def convert(value: U)(df: DataFrame): T

  def convertAny(value: Any)(df: DataFrame): T = convert(value.asInstanceOf[U])(df)

  /**
   * @return Wrappers for nested parameters
   */
  def nestedWrappers: Seq[SparkParamWrapper[_, _]] = Seq.empty
}
