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

import org.apache.spark.rdd.RDD

object AggregatorBatch {

  /**
    * Class used for providing option to access different return type per each bundled
    * aggregator in type-safe way.
    */
  case class BatchedResult(rawResults: Map[Aggregator[_, _], Any]) {

    def forAggregator[U, _](aggregator: Aggregator[U, _]): U = {
      rawResults(aggregator).asInstanceOf[U]
    }
  }

  def executeInBatch[T](
      rdd: RDD[T],
      aggregators: Seq[Aggregator[_, T]]): BatchedResult = {
    val batch = SplitterAggregator[Any, T](aggregators.map(_.asInstanceOf[Aggregator[Any, T]]))
    val results = batch.execute(rdd)

    val rawResultsMap: Map[Aggregator[_, _], Any] = (aggregators zip results).map {
      case (aggregator, result) => aggregator -> result
    }.toMap
    BatchedResult(rawResultsMap)
  }

  private case class SplitterAggregator[U, T](aggregators: Seq[Aggregator[U, T]])
    extends Aggregator[Seq[U], T] {

    override def initialElement: Seq[U] = aggregators.map(_.initialElement)

    override def mergeValue(accSeq: Seq[U], elem: T): Seq[U] = {
      (accSeq, aggregators).zipped.map { (acc, aggregator) =>
        aggregator.mergeValue(acc, elem)
      }
    }

    override def mergeCombiners(leftSeq: Seq[U], rightSeq: Seq[U]): Seq[U] = {
      (leftSeq, rightSeq, aggregators).zipped.map { (left, right, aggregator) =>
        aggregator.mergeCombiners(left, right)
      }
    }
  }
}
