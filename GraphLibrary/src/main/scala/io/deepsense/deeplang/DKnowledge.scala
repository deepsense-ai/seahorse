/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

/**
  */
class DKnowledge[T <: DOperable](val types: Set[T]) {
  def this(args: T*) = this(Set[T](args: _*))

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

  implicit def asCovariant[T <: DOperable, U <: T](dk: DKnowledge[U]): DKnowledge[T] = {
    DKnowledge(dk.types.asInstanceOf[Set[T]])
  }
}
