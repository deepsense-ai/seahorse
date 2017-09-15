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
}

object DKnowledge {
  implicit def asCovariant[T <: DOperable, U <: T](dk: DKnowledge[U]): DKnowledge[T] = {
    new DKnowledge(dk.types.asInstanceOf[Set[T]])
  }
}
