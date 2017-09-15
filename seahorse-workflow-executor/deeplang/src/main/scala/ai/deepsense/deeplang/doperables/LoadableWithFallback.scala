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

package ai.deepsense.deeplang.doperables

import org.apache.spark.ml

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.serialization.{CustomPersistence, SerializableSparkModel}

/**
  * This is a trait that lets define a method for loading model,
  * and if it fails, it falls back to default load implementation.
  * It is especially useful for supporting two spark versions.
  */
trait LoadableWithFallback[M <: ml.Model[M], E <: ml.Estimator[M]] { self: SparkModelWrapper[M, E] =>
  def tryToLoadModel(path: String): Option[M]

  protected def loadModel(ctx: ExecutionContext, path: String): SerializableSparkModel[M] = {
    tryToLoadModel(path) match {
      case Some(m) => new SerializableSparkModel(m)
      case None =>
        val modelPath = Transformer.modelFilePath(path)
        CustomPersistence.load[SerializableSparkModel[M]](ctx.sparkContext, modelPath)
    }
  }
}
