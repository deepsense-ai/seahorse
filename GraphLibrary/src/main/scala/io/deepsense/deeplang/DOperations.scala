/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

import ToVectorConversions._

/**
 * Implicit conversions used to convert results of _execute and _inferKnowledge to Vectors
 */
private object ToVectorConversions {

  implicit def singleValueToVector[T1 <: DOperable](t: T1): Vector[DOperable] = {
    Vector(t)
  }

  implicit def tuple2ToVector[T1 <: DOperable, T2 <: DOperable](
      t: (T1, T2)): Vector[DOperable] = {
    Vector(t._1, t._2)
  }

  implicit def tuple3ToVector[T1 <: DOperable, T2 <: DOperable, T3 <: DOperable](
      t: (T1, T2, T3)): Vector[DOperable] = {
    Vector(t._1, t._2, t._3)
  }

  implicit def dKnowledgeSingletonToVector[T1 <: DOperable](
      t: DKnowledge[T1]): Vector[DKnowledge[DOperable]] = {
    Vector(t)
  }

  implicit def dKnowledgeTuple2ToVector[T1 <: DOperable, T2 <: DOperable](
      t: (DKnowledge[T1], DKnowledge[T2])): Vector[DKnowledge[DOperable]] = {
    Vector(t._1, t._2)
  }

  implicit def dKnowledgeTuple3ToVector[T1 <: DOperable, T2 <: DOperable, T3 <: DOperable](
      t: (DKnowledge[T1], DKnowledge[T2], DKnowledge[T3])): Vector[DKnowledge[DOperable]] = {
    Vector(t._1, t._2, t._3)
  }
}

/** Following classes are generated automatically. */

abstract class DOperation0To1[
    TO_0 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 0
  val outArity = 1

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)()
  }

  protected def _execute(): TO_0

  protected def _inferKnowledge(context: InferContext)(): DKnowledge[TO_0] = {
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0])
  }
}

abstract class DOperation0To2[
    TO_0 <: DOperable : ru.TypeTag,
    TO_1 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 0
  val outArity = 2

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case 1 => ru.typeTag[TO_1]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)()
  }

  protected def _execute(): (TO_0, TO_1)

  protected def _inferKnowledge(context: InferContext)(): (DKnowledge[TO_0], DKnowledge[TO_1]) = {(
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_1]))
  }
}

abstract class DOperation0To3[
    TO_0 <: DOperable : ru.TypeTag,
    TO_1 <: DOperable : ru.TypeTag,
    TO_2 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 0
  val outArity = 3

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case 1 => ru.typeTag[TO_1]
      case 2 => ru.typeTag[TO_2]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)()
  }

  protected def _execute(): (TO_0, TO_1, TO_2)

  protected def _inferKnowledge(context: InferContext)(): (DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]) = {(
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_1]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_2]))
  }
}

abstract class DOperation1To0[
    TI_0 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 1
  val outArity = 0

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0])
    Vector()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]])
    Vector()
  }

  protected def _execute(
      t0: TI_0): Unit

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): Unit = {
  }
}

abstract class DOperation1To1[
    TI_0 <: DOperable : ru.TypeTag,
    TO_0 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 1
  val outArity = 1

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]])
  }

  protected def _execute(
      t0: TI_0): TO_0

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): DKnowledge[TO_0] = {
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0])
  }
}

abstract class DOperation1To2[
    TI_0 <: DOperable : ru.TypeTag,
    TO_0 <: DOperable : ru.TypeTag,
    TO_1 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 1
  val outArity = 2

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case 1 => ru.typeTag[TO_1]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]])
  }

  protected def _execute(
      t0: TI_0): (TO_0, TO_1)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): (DKnowledge[TO_0], DKnowledge[TO_1]) = {(
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_1]))
  }
}

abstract class DOperation1To3[
    TI_0 <: DOperable : ru.TypeTag,
    TO_0 <: DOperable : ru.TypeTag,
    TO_1 <: DOperable : ru.TypeTag,
    TO_2 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 1
  val outArity = 3

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case 1 => ru.typeTag[TO_1]
      case 2 => ru.typeTag[TO_2]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]])
  }

  protected def _execute(
      t0: TI_0): (TO_0, TO_1, TO_2)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): (DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]) = {(
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_1]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_2]))
  }
}

abstract class DOperation2To0[
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 2
  val outArity = 0

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case 1 => ru.typeTag[TI_1]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1])
    Vector()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]])
    Vector()
  }

  protected def _execute(
      t0: TI_0,
      t1: TI_1): Unit

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): Unit = {
  }
}

abstract class DOperation2To1[
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TO_0 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 2
  val outArity = 1

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case 1 => ru.typeTag[TI_1]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]])
  }

  protected def _execute(
      t0: TI_0,
      t1: TI_1): TO_0

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): DKnowledge[TO_0] = {
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0])
  }
}

abstract class DOperation2To2[
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TO_0 <: DOperable : ru.TypeTag,
    TO_1 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 2
  val outArity = 2

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case 1 => ru.typeTag[TI_1]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case 1 => ru.typeTag[TO_1]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]])
  }

  protected def _execute(
      t0: TI_0,
      t1: TI_1): (TO_0, TO_1)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): (DKnowledge[TO_0], DKnowledge[TO_1]) = {(
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_1]))
  }
}

abstract class DOperation2To3[
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TO_0 <: DOperable : ru.TypeTag,
    TO_1 <: DOperable : ru.TypeTag,
    TO_2 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 2
  val outArity = 3

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case 1 => ru.typeTag[TI_1]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case 1 => ru.typeTag[TO_1]
      case 2 => ru.typeTag[TO_2]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]])
  }

  protected def _execute(
      t0: TI_0,
      t1: TI_1): (TO_0, TO_1, TO_2)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): (DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]) = {(
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_1]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_2]))
  }
}

abstract class DOperation3To0[
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TI_2 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 3
  val outArity = 0

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case 1 => ru.typeTag[TI_1]
      case 2 => ru.typeTag[TI_2]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1],
      arguments(2).asInstanceOf[TI_2])
    Vector()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]],
      knowledge(2).asInstanceOf[DKnowledge[TI_2]])
    Vector()
  }

  protected def _execute(
      t0: TI_0,
      t1: TI_1,
      t2: TI_2): Unit

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): Unit = {
  }
}

abstract class DOperation3To1[
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TI_2 <: DOperable : ru.TypeTag,
    TO_0 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 3
  val outArity = 1

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case 1 => ru.typeTag[TI_1]
      case 2 => ru.typeTag[TI_2]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1],
      arguments(2).asInstanceOf[TI_2])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]],
      knowledge(2).asInstanceOf[DKnowledge[TI_2]])
  }

  protected def _execute(
      t0: TI_0,
      t1: TI_1,
      t2: TI_2): TO_0

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): DKnowledge[TO_0] = {
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0])
  }
}

abstract class DOperation3To2[
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TI_2 <: DOperable : ru.TypeTag,
    TO_0 <: DOperable : ru.TypeTag,
    TO_1 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 3
  val outArity = 2

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case 1 => ru.typeTag[TI_1]
      case 2 => ru.typeTag[TI_2]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case 1 => ru.typeTag[TO_1]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1],
      arguments(2).asInstanceOf[TI_2])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]],
      knowledge(2).asInstanceOf[DKnowledge[TI_2]])
  }

  protected def _execute(
      t0: TI_0,
      t1: TI_1,
      t2: TI_2): (TO_0, TO_1)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): (DKnowledge[TO_0], DKnowledge[TO_1]) = {(
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_1]))
  }
}

abstract class DOperation3To3[
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TI_2 <: DOperable : ru.TypeTag,
    TO_0 <: DOperable : ru.TypeTag,
    TO_1 <: DOperable : ru.TypeTag,
    TO_2 <: DOperable : ru.TypeTag] (
    parameters: DParameters)
  extends DOperation(parameters) {
  val inArity = 3
  val outArity = 3

  override def inPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TI_0]
      case 1 => ru.typeTag[TI_1]
      case 2 => ru.typeTag[TI_2]
      case _ => throw new IllegalArgumentException("Wrong input port index")
    }
  }

  override def outPortType(index: Int): ru.TypeTag[_] = {
    index match {
      case 0 => ru.typeTag[TO_0]
      case 1 => ru.typeTag[TO_1]
      case 2 => ru.typeTag[TO_2]
      case _ => throw new IllegalArgumentException("Wrong output port index")
    }
  }

  override def execute(arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1],
      arguments(2).asInstanceOf[TI_2])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]],
      knowledge(2).asInstanceOf[DKnowledge[TI_2]])
  }

  protected def _execute(
      t0: TI_0,
      t1: TI_1,
      t2: TI_2): (TO_0, TO_1, TO_2)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): (DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]) = {(
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_1]),
    DKnowledge(context.dHierarchy.concreteSubclassesInstances[TO_2]))
  }
}
