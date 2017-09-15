/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

abstract class DMethod0To1[P, TO0 <: DOperable] extends DMethod {
  def apply(parameters: P): TO0

  def infer(parameters: P): DKnowledge[TO0]
}

abstract class DMethod1To0[P, TI0 <: DOperable] extends DMethod {
  def apply(parameters: P)(t1: TI0): Unit

  def infer(parameters: P)(k: DKnowledge[TI0]): Unit
}

abstract class DMethod1To1[P, TI0 <: DOperable, TO0 <: DOperable] extends DMethod {
  def apply(parameters: P)(t0: TI0): TO0

  def infer(parameters: P)(k0: DKnowledge[TI0]): DKnowledge[TO0]
}

// TODO create more DMethods
