/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
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

  override def hashCode(): Int = types.hashCode()
}

object DKnowledge {
  def apply[T <: DOperable](args: T*) = new DKnowledge[T](args: _*)

  def apply[T <: DOperable](types: Set[T]) = new DKnowledge[T](types)

  def apply[T <: DOperable](dKnowledges: Traversable[DKnowledge[T]]): DKnowledge[T] =
    dKnowledges.foldLeft(DKnowledge[T]())(_ ++ _)

  implicit def asCovariant[T <: DOperable, U <: T](dk: DKnowledge[U]): DKnowledge[T] = {
    DKnowledge(dk.types.asInstanceOf[Set[T]])
  }
}
