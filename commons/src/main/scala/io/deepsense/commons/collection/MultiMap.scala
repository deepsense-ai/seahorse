/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.collection

object MultiMap {
  type MultiMap[A, B] = Map[A, Set[B]]

  implicit class MultiMapOpts[A, B](self: MultiMap[A, B]) {
    def addBinding(key: A, value: B): MultiMap[A, B] = {
      val newValueSet = self.get(key) match {
        case Some(defined) => defined + value
        case None => Set(value)
      }
      self + (key -> newValueSet)
    }

    def removeBinding(key: A, value: B): MultiMap[A, B] = {
      val newValueSet = self.get(key) match {
        case Some(defined) => defined - value
        case None => Set.empty[B]
      }
      if (newValueSet.nonEmpty) self + (key -> newValueSet) else self
    }
  }
}
