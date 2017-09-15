/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 *
 * DOperations and DClasses necessary to mock Graph.
 * TODO: This should be removed as soon, as it is possible to replace it with real implementations.
 */
package io.deepsense.graphexecutor

import io.deepsense.deeplang._
import io.deepsense.deeplang.parameters.ParametersSchema

object DClassesForDOperations {
  trait A extends DOperable
  class A1 extends A {
    override def equals(any: Any) = any.isInstanceOf[A1]
  }
  class A2 extends A {
    override def equals(any: Any) = any.isInstanceOf[A2]
  }
}

object DOperationTestClasses {
  import io.deepsense.graphexecutor.DClassesForDOperations._

  class DOperation0To1Test extends DOperation0To1[A1] {
    override protected def _execute(context: ExecutionContext)(): A1 = {
      Thread.sleep(10000)
      new A1()
    }

    override val name: String = ""

    override val parameters = ParametersSchema()
  }

  class DOperation1To0Test extends DOperation1To0[A1] {
    override protected def _execute(context: ExecutionContext)(t0: A1): Unit = {
      Thread.sleep(10000)
    }

    override val name: String = ""

    override val parameters = ParametersSchema()
  }

  class DOperation1To1Test extends DOperation1To1[A1, A] {
    override protected def _execute(context: ExecutionContext)(t0: A1): A = {
      Thread.sleep(10000)
      new A1()
    }

    override val name: String = ""

    override val parameters = ParametersSchema()
  }

  class DOperation2To1Test extends DOperation2To1[A1, A2, A] {
    override protected def _execute(context: ExecutionContext)(t0: A1, t1: A2): A = {
      Thread.sleep(10000)
      new A1()
    }

    override val name: String = ""

    override val parameters = ParametersSchema()
  }
}
