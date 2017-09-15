/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.ToVectorConversions._
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}

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
      t: (DKnowledge[T1], InferenceWarnings))
      : (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    (Vector(t._1), t._2)
  }

  implicit def dKnowledgeTuple2ToVector[T1 <: DOperable, T2 <: DOperable](
      t: ((DKnowledge[T1], DKnowledge[T2]), InferenceWarnings))
      : (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    val (k, w) = t
    (Vector(k._1, k._2), w)
  }

  implicit def dKnowledgeTuple3ToVector[T1 <: DOperable, T2 <: DOperable, T3 <: DOperable](
      t: ((DKnowledge[T1], DKnowledge[T2], DKnowledge[T3]), InferenceWarnings))
      : (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    val (k, w) = t
    (Vector(k._1, k._2, k._3), w)
  }
}

/** Following classes are generated automatically. */
// scalastyle:off

abstract class DOperation0To1[
    TO_0 <: DOperable]
  extends DOperation {
  val inArity = 0
  val outArity = 1


  def tTagTO_0: ru.TypeTag[TO_0]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector()

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)()
  }

  protected def _execute(context: ExecutionContext)(): TO_0

  protected def _inferKnowledge(context: InferContext)(): (DKnowledge[TO_0], InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)()
    } else {
      _inferTypeKnowledge(context)()
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(): (DKnowledge[TO_0], InferenceWarnings) = {(
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(): (DKnowledge[TO_0], InferenceWarnings) = {
    _inferTypeKnowledge(context)()
  }
}

abstract class DOperation0To2[
    TO_0 <: DOperable,
    TO_1 <: DOperable]
  extends DOperation {
  val inArity = 0
  val outArity = 2


  def tTagTO_0: ru.TypeTag[TO_0]

  def tTagTO_1: ru.TypeTag[TO_1]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector()

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0),
    ru.typeTag[TO_1](tTagTO_1))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)()
  }

  protected def _execute(context: ExecutionContext)(): (TO_0, TO_1)

  protected def _inferKnowledge(context: InferContext)(): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)()
    } else {
      _inferTypeKnowledge(context)()
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1](tTagTO_1))), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {
    _inferTypeKnowledge(context)()
  }
}

abstract class DOperation0To3[
    TO_0 <: DOperable,
    TO_1 <: DOperable,
    TO_2 <: DOperable]
  extends DOperation {
  val inArity = 0
  val outArity = 3


  def tTagTO_0: ru.TypeTag[TO_0]

  def tTagTO_1: ru.TypeTag[TO_1]

  def tTagTO_2: ru.TypeTag[TO_2]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector()

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0),
    ru.typeTag[TO_1](tTagTO_1),
    ru.typeTag[TO_2](tTagTO_2))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)()
  }

  protected def _execute(context: ExecutionContext)(): (TO_0, TO_1, TO_2)

  protected def _inferKnowledge(context: InferContext)(): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)()
    } else {
      _inferTypeKnowledge(context)()
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1](tTagTO_1)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_2](tTagTO_2))), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {
    _inferTypeKnowledge(context)()
  }
}

abstract class DOperation1To0[
    TI_0 <: DOperable]
  extends DOperation {
  val inArity = 1
  val outArity = 0


  def tTagTI_0: ru.TypeTag[TI_0]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector()

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0])
    Vector()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]])
    (Vector(), InferenceWarnings.empty)
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0): Unit

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): (Unit, InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0)
    } else {
      _inferTypeKnowledge(context)(k0)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): (Unit, InferenceWarnings) = {((), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): (Unit, InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0)
  }
}

abstract class DOperation1To1[
    TI_0 <: DOperable,
    TO_0 <: DOperable]
  extends DOperation {
  val inArity = 1
  val outArity = 1


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTO_0: ru.TypeTag[TO_0]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]])
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0): TO_0

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): (DKnowledge[TO_0], InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0)
    } else {
      _inferTypeKnowledge(context)(k0)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): (DKnowledge[TO_0], InferenceWarnings) = {(
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): (DKnowledge[TO_0], InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0)
  }
}

abstract class DOperation1To2[
    TI_0 <: DOperable,
    TO_0 <: DOperable,
    TO_1 <: DOperable]
  extends DOperation {
  val inArity = 1
  val outArity = 2


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTO_0: ru.TypeTag[TO_0]

  def tTagTO_1: ru.TypeTag[TO_1]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0),
    ru.typeTag[TO_1](tTagTO_1))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]])
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0): (TO_0, TO_1)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0)
    } else {
      _inferTypeKnowledge(context)(k0)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1](tTagTO_1))), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0)
  }
}

abstract class DOperation1To3[
    TI_0 <: DOperable,
    TO_0 <: DOperable,
    TO_1 <: DOperable,
    TO_2 <: DOperable]
  extends DOperation {
  val inArity = 1
  val outArity = 3


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTO_0: ru.TypeTag[TO_0]

  def tTagTO_1: ru.TypeTag[TO_1]

  def tTagTO_2: ru.TypeTag[TO_2]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0),
    ru.typeTag[TO_1](tTagTO_1),
    ru.typeTag[TO_2](tTagTO_2))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]])
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0): (TO_0, TO_1, TO_2)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0)
    } else {
      _inferTypeKnowledge(context)(k0)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1](tTagTO_1)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_2](tTagTO_2))), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0)
  }
}

abstract class DOperation2To0[
    TI_0 <: DOperable,
    TI_1 <: DOperable]
  extends DOperation {
  val inArity = 2
  val outArity = 0


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTI_1: ru.TypeTag[TI_1]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0),
    ru.typeTag[TI_1](tTagTI_1))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector()

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1])
    Vector()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]])
    (Vector(), InferenceWarnings.empty)
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0,
      t1: TI_1): Unit

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): (Unit, InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0, k1)
    } else {
      _inferTypeKnowledge(context)(k0, k1)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): (Unit, InferenceWarnings) = {((), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): (Unit, InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0, k1)
  }
}

abstract class DOperation2To1[
    TI_0 <: DOperable,
    TI_1 <: DOperable,
    TO_0 <: DOperable]
  extends DOperation {
  val inArity = 2
  val outArity = 1


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTI_1: ru.TypeTag[TI_1]

  def tTagTO_0: ru.TypeTag[TO_0]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0),
    ru.typeTag[TI_1](tTagTI_1))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]])
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0,
      t1: TI_1): TO_0

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): (DKnowledge[TO_0], InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0, k1)
    } else {
      _inferTypeKnowledge(context)(k0, k1)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): (DKnowledge[TO_0], InferenceWarnings) = {(
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): (DKnowledge[TO_0], InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0, k1)
  }
}

abstract class DOperation2To2[
    TI_0 <: DOperable,
    TI_1 <: DOperable,
    TO_0 <: DOperable,
    TO_1 <: DOperable]
  extends DOperation {
  val inArity = 2
  val outArity = 2


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTI_1: ru.TypeTag[TI_1]

  def tTagTO_0: ru.TypeTag[TO_0]

  def tTagTO_1: ru.TypeTag[TO_1]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0),
    ru.typeTag[TI_1](tTagTI_1))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0),
    ru.typeTag[TO_1](tTagTO_1))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]])
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0,
      t1: TI_1): (TO_0, TO_1)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0, k1)
    } else {
      _inferTypeKnowledge(context)(k0, k1)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1](tTagTO_1))), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0, k1)
  }
}

abstract class DOperation2To3[
    TI_0 <: DOperable,
    TI_1 <: DOperable,
    TO_0 <: DOperable,
    TO_1 <: DOperable,
    TO_2 <: DOperable]
  extends DOperation {
  val inArity = 2
  val outArity = 3


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTI_1: ru.TypeTag[TI_1]

  def tTagTO_0: ru.TypeTag[TO_0]

  def tTagTO_1: ru.TypeTag[TO_1]

  def tTagTO_2: ru.TypeTag[TO_2]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0),
    ru.typeTag[TI_1](tTagTI_1))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0),
    ru.typeTag[TO_1](tTagTO_1),
    ru.typeTag[TO_2](tTagTO_2))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]])
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0,
      t1: TI_1): (TO_0, TO_1, TO_2)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0, k1)
    } else {
      _inferTypeKnowledge(context)(k0, k1)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1](tTagTO_1)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_2](tTagTO_2))), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0, k1)
  }
}

abstract class DOperation3To0[
    TI_0 <: DOperable,
    TI_1 <: DOperable,
    TI_2 <: DOperable]
  extends DOperation {
  val inArity = 3
  val outArity = 0


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTI_1: ru.TypeTag[TI_1]

  def tTagTI_2: ru.TypeTag[TI_2]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0),
    ru.typeTag[TI_1](tTagTI_1),
    ru.typeTag[TI_2](tTagTI_2))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector()

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1],
      arguments(2).asInstanceOf[TI_2])
    Vector()
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]],
      knowledge(2).asInstanceOf[DKnowledge[TI_2]])
    (Vector(), InferenceWarnings.empty)
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0,
      t1: TI_1,
      t2: TI_2): Unit

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): (Unit, InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0, k1, k2)
    } else {
      _inferTypeKnowledge(context)(k0, k1, k2)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): (Unit, InferenceWarnings) = {((), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): (Unit, InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0, k1, k2)
  }
}

abstract class DOperation3To1[
    TI_0 <: DOperable,
    TI_1 <: DOperable,
    TI_2 <: DOperable,
    TO_0 <: DOperable]
  extends DOperation {
  val inArity = 3
  val outArity = 1


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTI_1: ru.TypeTag[TI_1]

  def tTagTI_2: ru.TypeTag[TI_2]

  def tTagTO_0: ru.TypeTag[TO_0]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0),
    ru.typeTag[TI_1](tTagTI_1),
    ru.typeTag[TI_2](tTagTI_2))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1],
      arguments(2).asInstanceOf[TI_2])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]],
      knowledge(2).asInstanceOf[DKnowledge[TI_2]])
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0,
      t1: TI_1,
      t2: TI_2): TO_0

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): (DKnowledge[TO_0], InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0, k1, k2)
    } else {
      _inferTypeKnowledge(context)(k0, k1, k2)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): (DKnowledge[TO_0], InferenceWarnings) = {(
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): (DKnowledge[TO_0], InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0, k1, k2)
  }
}

abstract class DOperation3To2[
    TI_0 <: DOperable,
    TI_1 <: DOperable,
    TI_2 <: DOperable,
    TO_0 <: DOperable,
    TO_1 <: DOperable]
  extends DOperation {
  val inArity = 3
  val outArity = 2


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTI_1: ru.TypeTag[TI_1]

  def tTagTI_2: ru.TypeTag[TI_2]

  def tTagTO_0: ru.TypeTag[TO_0]

  def tTagTO_1: ru.TypeTag[TO_1]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0),
    ru.typeTag[TI_1](tTagTI_1),
    ru.typeTag[TI_2](tTagTI_2))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0),
    ru.typeTag[TO_1](tTagTO_1))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1],
      arguments(2).asInstanceOf[TI_2])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]],
      knowledge(2).asInstanceOf[DKnowledge[TI_2]])
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0,
      t1: TI_1,
      t2: TI_2): (TO_0, TO_1)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0, k1, k2)
    } else {
      _inferTypeKnowledge(context)(k0, k1, k2)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1](tTagTO_1))), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0, k1, k2)
  }
}

abstract class DOperation3To3[
    TI_0 <: DOperable,
    TI_1 <: DOperable,
    TI_2 <: DOperable,
    TO_0 <: DOperable,
    TO_1 <: DOperable,
    TO_2 <: DOperable]
  extends DOperation {
  val inArity = 3
  val outArity = 3


  def tTagTI_0: ru.TypeTag[TI_0]

  def tTagTI_1: ru.TypeTag[TI_1]

  def tTagTI_2: ru.TypeTag[TI_2]

  def tTagTO_0: ru.TypeTag[TO_0]

  def tTagTO_1: ru.TypeTag[TO_1]

  def tTagTO_2: ru.TypeTag[TO_2]

  @transient
  override lazy val inPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TI_0](tTagTI_0),
    ru.typeTag[TI_1](tTagTI_1),
    ru.typeTag[TI_2](tTagTI_2))

  @transient
  override lazy val outPortTypes: Vector[ru.TypeTag[_]] = Vector(
    ru.typeTag[TO_0](tTagTO_0),
    ru.typeTag[TO_1](tTagTO_1),
    ru.typeTag[TO_2](tTagTO_2))

  override def execute(context: ExecutionContext)(
      arguments: Vector[DOperable]): Vector[DOperable] = {
    _execute(context)(
      arguments(0).asInstanceOf[TI_0],
      arguments(1).asInstanceOf[TI_1],
      arguments(2).asInstanceOf[TI_2])
  }

  override def inferKnowledge(context: InferContext)(
      knowledge: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = {
    _inferKnowledge(context)(
      knowledge(0).asInstanceOf[DKnowledge[TI_0]],
      knowledge(1).asInstanceOf[DKnowledge[TI_1]],
      knowledge(2).asInstanceOf[DKnowledge[TI_2]])
  }

  protected def _execute(context: ExecutionContext)(
      t0: TI_0,
      t1: TI_1,
      t2: TI_2): (TO_0, TO_1, TO_2)

  protected def _inferKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {
    if (context.fullInference) {
      _inferFullKnowledge(context)(k0, k1, k2)
    } else {
      _inferTypeKnowledge(context)(k0, k1, k2)
    }
  }

  protected def _inferTypeKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0](tTagTO_0)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1](tTagTO_1)),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_2](tTagTO_2))), InferenceWarnings.empty)
  }

  protected def _inferFullKnowledge(context: InferContext)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = {
    _inferTypeKnowledge(context)(k0, k1, k2)
  }
}

// scalastyle:on
