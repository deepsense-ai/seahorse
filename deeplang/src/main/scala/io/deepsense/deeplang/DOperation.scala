/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.parameters.ParametersSchema

/**
 * DOperation that receives and returns instances of DOperable.
 * Can infer its output type basing on type knowledge.
 */
@SerialVersionUID(1L)
abstract class DOperation extends Serializable {
  val inArity: Int
  val outArity: Int
  val name: String
  val parameters: ParametersSchema

  def inPortTypes: Vector[ru.TypeTag[_]]

  def outPortTypes: Vector[ru.TypeTag[_]]

  def execute(context: ExecutionContext)(l: Vector[DOperable]): Vector[DOperable]

  def inferKnowledge(context: InferContext)
                    (l: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]]
}
