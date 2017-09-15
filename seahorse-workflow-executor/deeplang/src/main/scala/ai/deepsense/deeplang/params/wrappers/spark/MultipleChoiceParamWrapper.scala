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

package ai.deepsense.deeplang.params.wrappers.spark

import scala.reflect.runtime.universe._

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.params.choice.{Choice, MultipleChoiceParam}

class MultipleChoiceParamWrapper[P <: ml.param.Params, T <: Choice : TypeTag](
    override val name: String,
    override val description: Option[String],
    val sparkParamGetter: P => ml.param.StringArrayParam)
  extends MultipleChoiceParam[T](name, description)
  with SparkParamWrapper[P, Array[String], Set[T]] {

  override def convert(value: Set[T])(schema: StructType): Array[String] = value.map(_.name).toArray

  override def replicate(name: String): MultipleChoiceParamWrapper[P, T] =
    new MultipleChoiceParamWrapper[P, T](name, description, sparkParamGetter)
}
