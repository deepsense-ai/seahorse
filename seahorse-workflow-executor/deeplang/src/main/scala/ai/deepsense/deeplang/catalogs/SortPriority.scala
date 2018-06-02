/**
 * Copyright 2018 Astraea, Inc.
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

package ai.deepsense.deeplang.catalogs

/**
  * Type used to declare a priority based ordering.
  */
case class SortPriority(value: Int) { self =>
  def next(skip: Int): SortPriority = SortPriority(value + skip)
  def inSequence(skip: Int): Iterator[SortPriority] = new Iterator[SortPriority]() {
    private var curr = self
    override def hasNext: Boolean = true
    override def next(): SortPriority = {
      val retval = curr
      curr = curr.next(skip)
      retval
    }
  }
}

object SortPriority {
  /** Lowest possible priority. */
  def MINIMUM = SortPriority(Int.MinValue)
  /** Default priority value for "don't care" cases. */
  def DEFAULT = SortPriority(0)
  /** Priority for items collected by the CatalogScanner. */
  def REFLECTED_BASE = SortPriority(1 << 20)
  /** Highest possible priority. */
  def MAXIMUM = SortPriority(Int.MaxValue)

  /** Typeclass for sorting/ordering priority specifications. */
  implicit val sortPriorityOrdering: Ordering[SortPriority] = Ordering.by[SortPriority, Int](_.value)
}
