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

package ai.deepsense.commons.utils

import scala.math.Ordering

object CollectionExtensions {

  implicit class RichSeq[T](seq: Seq[T]) {

    def hasUniqueValues: Boolean = seq.distinct.size == seq.size

    def hasDuplicates: Boolean = !hasUniqueValues

    /**
     * Works like groupBy, but assumes function f is injective, so there is
     * only one element for each key.
     */
    def lookupBy[R](f: T => R): Map[R, T] = {
      val mapEntries = seq.map(e => f(e) -> e)
      assert(mapEntries.size == seq.size,
        "Function f must be injective, otherwise we would override some key")
      mapEntries.toMap
    }

    def isSorted(implicit ord: Ordering[T]): Boolean = seq == seq.sorted

  }

  implicit class RichSet[T](set: Set[T]) {

    /**
      * Returns set with elements which are in both sets but not in their intersection
      */
    def xor(another: Set[T]): Set[T] = (set diff another) union (another diff set)

  }

}
