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

package ai.deepsense.deeplang.utils.aggregators

import ai.deepsense.deeplang.{DeeplangIntegTestSupport, UnitSpec}

case class SetAggregator() extends Aggregator[Set[Int], Int] {
  override def initialElement: Set[Int] = Set.empty
  override def mergeValue(acc: Set[Int], elem: Int): Set[Int] = acc + elem
  override def mergeCombiners(left: Set[Int], right: Set[Int]): Set[Int] = left ++ right
}

case class SumAggregator() extends Aggregator[Int, Int] {
  override def initialElement: Int = 0
  override def mergeValue(acc: Int, elem: Int): Int = acc + elem
  override def mergeCombiners(left: Int, right: Int): Int = left + right
}

class AggregatorBatchTest extends DeeplangIntegTestSupport {

  "AggregatorBatch" should {

    "properly execute all aggregation operations for provided aggregators" in {
      val rdd = sparkContext.parallelize(Seq(1, 2, 3))

      val setAggregator = SetAggregator()
      val sumAggregator = SumAggregator()

      val results = AggregatorBatch.executeInBatch(rdd, Seq(setAggregator, sumAggregator))

      results.forAggregator(setAggregator) shouldEqual Set(1, 2, 3)
      results.forAggregator(sumAggregator) shouldEqual 6
    }

  }
}
