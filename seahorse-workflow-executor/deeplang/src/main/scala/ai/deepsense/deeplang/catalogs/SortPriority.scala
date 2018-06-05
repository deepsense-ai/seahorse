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
case class SortPriority(value: Int) extends Ordered[SortPriority]  { self =>
  /** Construct the next `SortPriority` `skip` values away from this one. */
  def next(skip: Int): SortPriority = SortPriority(value + skip)

  /** Construct the next core priority from this one */
  def nextCore(): SortPriority = next(100)

  /** Constructs an iterator generating a sequence of `SortPriority` with `skip` distance between them,
    * starting with this one. */
  def inSequence(skip: Int): Iterator[SortPriority] = new Iterator[SortPriority]() {
    private var curr = self
    override def hasNext: Boolean = true
    override def next(): SortPriority = {
      val retval = curr
      curr = curr.next(skip)
      retval
    }
  }

  override def compare(o: SortPriority): Int = {
    value.compare(o.value)
  }
}

object SortPriority {
  /** Lowest possible priority. */
  def lowerBound = SortPriority(Int.MinValue)
  /** Default priority value for "don't care" cases. */
  def coreDefault = SortPriority(0)
  /** Priority for items collected by the CatalogScanner. */
  def sdkDefault = SortPriority(1 << 20)
  /** Highest possible priority. */
  def upperBound = SortPriority(Int.MaxValue)

  /** returns generator for core Seahorse operations and categories priorities */
  def coreInSequence: Iterator[SortPriority] = coreDefault.inSequence(100)

  /** returns generator for SDK operations loaded using @Register annotation */
  def sdkInSequence: Iterator[SortPriority] = sdkDefault.inSequence(10)

  /** Typeclass for sorting/ordering priority specifications. */
  implicit val sortPriorityOrdering: Ordering[SortPriority] = Ordering.by[SortPriority, Int](_.value)
}
