/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import io.deepsense.deeplang.parameters.ParametersSchema

import scala.reflect.runtime.{universe => ru}

/**
 * DOperation that receives and returns instances of DOperable.
 * Can infer its output type basing on type knowledge.
 */
@SerialVersionUID(1L)
abstract class DOperation extends Serializable {
  val inArity: Int
  val outArity: Int
  val name: String
  var parameters: ParametersSchema = _

  def inPortTypes: Vector[ru.TypeTag[_]]

  def outPortTypes: Vector[ru.TypeTag[_]]

  def execute(context: ExecutionContext)(l: Vector[DOperable]): Vector[DOperable]

  def inferKnowledge(context: InferContext)
                    (l: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]]
}
