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

case class HistogramAggregator(buckets: Array[Double], evenBuckets: Boolean)
  extends Aggregator[Array[Long], Double] {

  // Code based on DoubleRDDFunctions::histogram method.
  // Changes:
  // Method mergeValue is called histogramPartition in DoubleRDDFunctions
  // Method mergeCombiners is called mergeCounters in DoubleRDDFunctions
  // Method mergeValue is reworked to operate on single elements rather than Array.

  // Basic bucket function. This works using Java's built in Array
  // binary search. Takes log(size(buckets))
  private def basicBucketFunction(e: Double): Option[Int] = {
    val location = java.util.Arrays.binarySearch(buckets, e)
    if (location < 0) {
      // If the location is less than 0 then the insertion point in the array
      // to keep it sorted is -location-1
      val insertionPoint = -location - 1
      // If we have to insert before the first element or after the last one
      // its out of bounds.
      // We do this rather than buckets.lengthCompare(insertionPoint)
      // because Array[Double] fails to override it (for now).
      if (insertionPoint > 0 && insertionPoint < buckets.length) {
        Some(insertionPoint - 1)
      } else {
        None
      }
    } else if (location < buckets.length - 1) {
      // Exact match, just insert here
      Some(location)
    } else {
      // Exact match to the last element
      Some(location - 1)
    }
  }

  // Determine the bucket function in constant time. Requires that buckets are evenly spaced
  private def fastBucketFunction(min: Double, max: Double, count: Int)(e: Double): Option[Int] = {
    // If our input is not a number unless the increment is also NaN then we fail fast
    if (e.isNaN || e < min || e > max) {
      None
    } else {
      // Compute ratio of e's distance along range to total range first, for better precision
      val bucketNumber = (((e - min) / (max - min)) * count).toInt
      // should be less than count, but will equal count if e == max, in which case
      // it's part of the last end-range-inclusive bucket, so return count-1
      Some(math.min(bucketNumber, count - 1))
    }
  }

  // Decide which bucket function to pass to histogramPartition. We decide here
  // rather than having a general function so that the decision need only be made
  // once rather than once per shard
  private val bucketFunction = if (evenBuckets) {
    fastBucketFunction(buckets.head, buckets.last, buckets.length - 1) _
  } else {
    basicBucketFunction _
  }

  override def mergeValue(r: Array[Long], t: Double): Array[Long] = {
    bucketFunction(t) match {
      case Some(x: Int) => r(x) += 1
      case _ => {}
    }
    r
  }

  override def mergeCombiners(a1: Array[Long], a2: Array[Long]): Array[Long] = {
    a1.indices.foreach(i => a1(i) += a2(i))
    a1
  }

  override def initialElement: Array[Long] = new Array[Long](buckets.length - 1)
}
