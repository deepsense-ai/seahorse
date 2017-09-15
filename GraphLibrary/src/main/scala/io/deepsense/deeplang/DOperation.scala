/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

/**
 * DOperation that receives and returns instances of DOperable.
 * Can infer its output type basing on type knowledge.
 */
abstract class DOperation(val parameters: DParameters) {
  val inArity: Int
  val outArity: Int

  def inPortType(index: Int): ru.TypeTag[_]

  def outPortType(index: Int): ru.TypeTag[_]

  def execute(l: Vector[DOperable]): Vector[DOperable]

  def inferKnowledge(context: InferContext)
                    (l: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]]
}
