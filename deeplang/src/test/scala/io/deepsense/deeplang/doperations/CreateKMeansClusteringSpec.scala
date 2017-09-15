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

package io.deepsense.deeplang.doperations

import org.apache.spark.mllib.clustering.KMeans

import io.deepsense.deeplang.doperables.machinelearning.kmeans.{KMeansParameters, UntrainedKMeansClustering}
import io.deepsense.deeplang.{ExecutionContext, UnitSpec}

class CreateKMeansClusteringSpec extends UnitSpec {
  "CreateKMeansClustering DOperation" should {
    "create UntrainedKMeansClustering" in {
      val createKMeansClustering =
        CreateKMeansClustering(2, 100, KMeans.RANDOM, Some(1), 123, 1, 1e-4)

      val context = mock[ExecutionContext]
      val resultVector = createKMeansClustering.execute(context)(Vector.empty)

      val untrainedKMeansClustering = resultVector.head.asInstanceOf[UntrainedKMeansClustering]

      // Note that "initialization steps" parameter is ignored in RANDOM mode.
      val expectedParameters = KMeansParameters(2, 100, KMeans.RANDOM, None, 123, 1, 1e-4)
      untrainedKMeansClustering.modelParameters shouldBe expectedParameters
    }
  }
}
