/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang

import scala.reflect.runtime.{universe => ru}

import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}

// code below is generated automatically
// scalastyle:off
abstract class DMethod0To1[
    P,
    +TO_0 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (): TO_0

  def infer(context: InferContext)(parameters: P)(): (DKnowledge[TO_0], InferenceWarnings) = { (
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]), InferenceWarnings.empty)
  }
}

abstract class DMethod0To2[
    P,
    +TO_0 <: DOperable : ru.TypeTag,
    +TO_1 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (): (TO_0, TO_1)

  def infer(context: InferContext)(parameters: P)(): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = { ((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1])), InferenceWarnings.empty)
  }
}

abstract class DMethod0To3[
    P,
    +TO_0 <: DOperable : ru.TypeTag,
    +TO_1 <: DOperable : ru.TypeTag,
    +TO_2 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (): (TO_0, TO_1, TO_2)

  def infer(context: InferContext)(parameters: P)(): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = { ((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_2])), InferenceWarnings.empty)
  }
}

abstract class DMethod1To0[
    P,
    TI_0 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0): Unit

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0]): (Unit, InferenceWarnings) = { ((), InferenceWarnings.empty)
  }
}

abstract class DMethod1To1[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    +TO_0 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0): TO_0

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0]): (DKnowledge[TO_0], InferenceWarnings) = { (
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]), InferenceWarnings.empty)
  }
}

abstract class DMethod1To2[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    +TO_0 <: DOperable : ru.TypeTag,
    +TO_1 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0): (TO_0, TO_1)

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = { ((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1])), InferenceWarnings.empty)
  }
}

abstract class DMethod1To3[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    +TO_0 <: DOperable : ru.TypeTag,
    +TO_1 <: DOperable : ru.TypeTag,
    +TO_2 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0): (TO_0, TO_1, TO_2)

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = { ((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_2])), InferenceWarnings.empty)
  }
}

abstract class DMethod2To0[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0, t1: TI_1): Unit

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): (Unit, InferenceWarnings) = { ((), InferenceWarnings.empty)
  }
}

abstract class DMethod2To1[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    +TO_0 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0, t1: TI_1): TO_0

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): (DKnowledge[TO_0], InferenceWarnings) = { (
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]), InferenceWarnings.empty)
  }
}

abstract class DMethod2To2[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    +TO_0 <: DOperable : ru.TypeTag,
    +TO_1 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0, t1: TI_1): (TO_0, TO_1)

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = { ((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1])), InferenceWarnings.empty)
  }
}

abstract class DMethod2To3[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    +TO_0 <: DOperable : ru.TypeTag,
    +TO_1 <: DOperable : ru.TypeTag,
    +TO_2 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0, t1: TI_1): (TO_0, TO_1, TO_2)

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = { ((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_2])), InferenceWarnings.empty)
  }
}

abstract class DMethod3To0[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TI_2 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0, t1: TI_1, t2: TI_2): Unit

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): (Unit, InferenceWarnings) = { ((), InferenceWarnings.empty)
  }
}

abstract class DMethod3To1[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TI_2 <: DOperable : ru.TypeTag,
    +TO_0 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0, t1: TI_1, t2: TI_2): TO_0

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): (DKnowledge[TO_0], InferenceWarnings) = { (
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]), InferenceWarnings.empty)
  }
}

abstract class DMethod3To2[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TI_2 <: DOperable : ru.TypeTag,
    +TO_0 <: DOperable : ru.TypeTag,
    +TO_1 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0, t1: TI_1, t2: TI_2): (TO_0, TO_1)

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): ((DKnowledge[TO_0], DKnowledge[TO_1]), InferenceWarnings) = { ((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1])), InferenceWarnings.empty)
  }
}

abstract class DMethod3To3[
    P,
    TI_0 <: DOperable : ru.TypeTag,
    TI_1 <: DOperable : ru.TypeTag,
    TI_2 <: DOperable : ru.TypeTag,
    +TO_0 <: DOperable : ru.TypeTag,
    +TO_1 <: DOperable : ru.TypeTag,
    +TO_2 <: DOperable : ru.TypeTag] extends DMethod {
  def apply(context: ExecutionContext)(parameters: P)
           (t0: TI_0, t1: TI_1, t2: TI_2): (TO_0, TO_1, TO_2)

  def infer(context: InferContext)(parameters: P)(
      k0: DKnowledge[TI_0],
      k1: DKnowledge[TI_1],
      k2: DKnowledge[TI_2]): ((DKnowledge[TO_0], DKnowledge[TO_1], DKnowledge[TO_2]), InferenceWarnings) = { ((
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_0]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_1]),
    DKnowledge(context.dOperableCatalog.concreteSubclassesInstances[TO_2])), InferenceWarnings.empty)
  }
}

// scalastyle:on
