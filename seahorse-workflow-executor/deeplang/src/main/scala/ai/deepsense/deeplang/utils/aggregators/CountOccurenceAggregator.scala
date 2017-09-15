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

case class CountOccurenceAggregator[T](elementToCount: T) extends Aggregator[Long, T] {

  override def initialElement: Long = 0

  override def mergeValue(acc: Long, elem: T): Long = {
    if (elem == elementToCount) {
      acc + 1
    } else {
      acc
    }
  }

  override def mergeCombiners(left: Long, right: Long): Long = left + right
}
