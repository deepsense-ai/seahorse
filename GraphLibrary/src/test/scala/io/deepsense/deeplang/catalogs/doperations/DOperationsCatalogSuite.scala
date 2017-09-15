/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperations

import scala.reflect.runtime.universe.TypeTag

import org.scalatest.FunSuite

import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperations.exceptions._

object DOperationCatalogTestResources {
  object CategoryTree {
    object IO extends DOperationCategory("Input/Output")

    object DataManipulation
      extends DOperationCategory("Data manipulation")

    object ML extends DOperationCategory("Machine learning") {

      object Regression extends DOperationCategory("Regression", ML)

      object Classification extends DOperationCategory("Classification", ML)

      object Clustering extends DOperationCategory("Clustering", ML)

      object Evaluation extends DOperationCategory("Evaluation", ML)
    }

    object Utils extends DOperationCategory("Utilities", None)
  }

  abstract class DOperationMock extends DOperation {
    def inPortTypes: Vector[TypeTag[_]] = Vector()

    def outPortTypes: Vector[TypeTag[_]] = Vector()

    def inferKnowledge(
        context: InferContext)(
        l: Vector[DKnowledge[DOperable]]): Vector[DKnowledge[DOperable]] = ???

    def execute(context: ExecutionContext)(l: Vector[DOperable]): Vector[DOperable] = ???
  }

  case class DOperationA() extends DOperationMock {
    override val inArity: Int = 2
    override val outArity: Int = 3
  }

  case class DOperationWithoutParameterLessConstructor(x: Int) extends DOperationMock {
    override val inArity: Int = 2
    override val outArity: Int = 3
  }
}

class DOperationsCatalogSuite extends FunSuite {

  test("It is possible to create instance of registered DOperation") {
    import io.deepsense.deeplang.catalogs.doperations.DOperationCatalogTestResources._
    val manager = DOperationsCatalog()
    val name = "name"
    manager.registerDOperation[DOperationA](name, CategoryTree.ML.Regression, "")
    val instance = manager.createDOperation(name)
    assert(instance == DOperationA())
  }

  test("Attempt of creating unregistered DOperation raises exception") {
    intercept[DOperationNotFoundException] {
      val manager = DOperationsCatalog()
      manager.createDOperation("unknown DOperation name")
    }
  }

  test("Registering DOperation without parameter-less constructor raises exception") {
    intercept[NoParameterLessConstructorInDOperationException] {
      import io.deepsense.deeplang.catalogs.doperations.DOperationCatalogTestResources._
      val manager = DOperationsCatalog()
      manager.registerDOperation[DOperationWithoutParameterLessConstructor](
        "name", CategoryTree.ML.Regression, "description")
    }
  }

  test("It is possible to get list of registered DOperations descriptors") {
    // TODO
  }

  test("It is possible to get tree of registered categories and DOperations") {
    // TODO
  }
}
