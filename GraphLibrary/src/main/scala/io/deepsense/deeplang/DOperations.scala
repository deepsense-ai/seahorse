/*
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

abstract class DOperation0To1[TO0 <: DOperable] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 0
  val outArity = 1

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    Vector(_execute())
  }

  override def inferTypes(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    Vector(_inferTypes())
  }

  protected def _execute(): TO0

  protected def _inferTypes(): DKnowledge[TO0]
}

abstract class DOperation1To1[TI0 <: DOperable, TO0 <: DOperable] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 1
  val outArity = 1

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    Vector(_execute(arguments(0).asInstanceOf[TI0]))
  }

  override def inferTypes(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    Vector(_inferTypes(knowledge(0).asInstanceOf[DKnowledge[TI0]]))
  }

  protected def _execute(t0: TI0): TO0

  protected def _inferTypes(k: DKnowledge[TI0]): DKnowledge[TO0]
}

// TODO define more DOPerations
