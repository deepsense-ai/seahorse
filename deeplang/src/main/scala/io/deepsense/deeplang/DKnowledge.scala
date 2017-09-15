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

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

class DKnowledge[T <: DOperable](val types: Set[T]) {
  def this(args: T*) = this(Set[T](args: _*))

  /**
   * Returns a DKnowledge with types that are subtypes of given Type.
   */
  def filterTypes(t: ru.Type): DKnowledge[T] = {
    DKnowledge(types.filter(x => TypeUtils.classToType(x.getClass) <:< t))
  }

  def ++[U >: T <: DOperable](other: DKnowledge[U]): DKnowledge[U] = {
    DKnowledge[U](types ++ other.types)
  }

  override def equals(other: Any): Boolean = {
    other match {
      case that: DKnowledge[_] => types == that.types
      case _ => false
    }
  }

  /**
   * TODO document and test
   */
  def single: T = {
    require(types.nonEmpty, "Expected at least one inferred type, but got 0")
    types.iterator.next()
  }

  lazy val size = types.size

  override def hashCode(): Int = types.hashCode()

  override def toString: String = s"DKnowledge($types)"
}

object DKnowledge {
  def apply[T <: DOperable](args: T*): DKnowledge[T] = new DKnowledge[T](args: _*)

  def apply[T <: DOperable](types: Set[T]): DKnowledge[T] = new DKnowledge[T](types)

  def apply[T <: DOperable](dKnowledges: Traversable[DKnowledge[T]]): DKnowledge[T] =
    dKnowledges.foldLeft(DKnowledge[T]())(_ ++ _)

  implicit def asCovariant[T <: DOperable, U <: T](dk: DKnowledge[U]): DKnowledge[T] = {
    DKnowledge(dk.types.asInstanceOf[Set[T]])
  }
}
