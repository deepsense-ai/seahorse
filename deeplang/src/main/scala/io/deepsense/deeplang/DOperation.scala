/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

import io.deepsense.commons.models
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.parameters.ParametersSchema

/**
 * DOperation that receives and returns instances of DOperable.
 * Can infer its output type based on type knowledge.
 */
@SerialVersionUID(1L)
abstract class DOperation extends Serializable with Logging {
  val inArity: Int
  val outArity: Int
  val id: DOperation.Id
  val name: String
  // NOTE: Default version value = "0.1.0"
  // NOTE: Currently version number is semantically ignored everywhere
  val version: String = "0.1.0"
  val parameters: ParametersSchema

  def inPortTypes: Vector[ru.TypeTag[_]]

  def outPortTypes: Vector[ru.TypeTag[_]]

  def execute(context: ExecutionContext)(l: Vector[DOperable]): Vector[DOperable]

  /**
   * Infers knowledge for this operation.
   * @param context Infer context to be used in inference.
   * @param inputKnowledge Vector of knowledge objects to be put in input ports of this operation.
   *                       This method assumes that size of this vector is equal to [[inArity]].
   * @return A tuple consisting of:
   *          - vector of knowledge object for each of operation's output port
   *          - inference warnings for this operation
   */
  def inferKnowledge(
      context: InferContext)(
      inputKnowledge: Vector[DKnowledge[DOperable]])
      : (Vector[DKnowledge[DOperable]], InferenceWarnings)
}

object DOperation {
  type Id = models.Id
  val Id = models.Id
}
