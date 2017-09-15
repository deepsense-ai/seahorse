/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

/**
 * DOperation that receives and returns instances of DOperable.
 * Can infer its output type basing on type knowledge.
 */
abstract class DOperation(val parameters: DParameters) {
  val inArity: Int
  val outArity: Int

  def execute(l: Vector[DOperable]): Vector[DOperable]

  def inferTypes(l: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]]
}
