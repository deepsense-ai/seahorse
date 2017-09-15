/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperations

import scala.reflect.runtime.universe.{TypeTag, typeTag}

import org.scalatest.{FunSuite, Matchers}

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

  case class X() extends DOperable
  case class Y() extends DOperable

  val XTypeTag = typeTag[X]
  val YTypeTag = typeTag[Y]

  case class DOperationA() extends DOperationMock {
    override val inArity: Int = 2
    override val outArity: Int = 3
  }

  case class DOperationB() extends DOperationMock {
    override val inArity: Int = 1
    override val outArity: Int = 2
  }

  case class DOperationC() extends DOperationMock {
    override val inArity: Int = 0
    override val outArity: Int = 1
  }

  case class DOperationD() extends DOperationMock {
    override val inArity: Int = 2
    override val outArity: Int = 1
    override val inPortTypes: Vector[TypeTag[_]] = Vector(XTypeTag, YTypeTag)
    override val outPortTypes: Vector[TypeTag[_]] = Vector(XTypeTag)
  }

  case class DOperationWithoutParameterlessConstructor(x: Int) extends DOperationMock {
    override val inArity: Int = 2
    override val outArity: Int = 3
  }
}

object ViewingTestResources {
  import DOperationCatalogTestResources._

  val nameA = "nameA"
  val nameB = "nameB"
  val nameC = "nameC"
  val nameD = "nameD"

  val descriptionA = "descriptionA"
  val descriptionB = "descriptionB"
  val descriptionC = "descriptionC"
  val descriptionD = "descriptionD"

  val categoryA = CategoryTree.ML.Regression
  val categoryB = CategoryTree.ML.Regression
  val categoryC = CategoryTree.ML.Classification
  val categoryD = CategoryTree.ML

  val catalog = DOperationsCatalog()

  catalog.registerDOperation[DOperationA](nameA, categoryA, descriptionA)
  catalog.registerDOperation[DOperationB](nameB, categoryB, descriptionB)
  catalog.registerDOperation[DOperationC](nameC, categoryC, descriptionC)
  catalog.registerDOperation[DOperationD](nameD, categoryD, descriptionD)

  val expectedA = DOperationDescriptor(nameA, descriptionA, categoryA, Nil, Nil)
  val expectedB = DOperationDescriptor(nameB, descriptionB, categoryB, Nil, Nil)
  val expectedC = DOperationDescriptor(nameC, descriptionC, categoryC, Nil, Nil)
  val expectedD = DOperationDescriptor(
    nameD, descriptionD, categoryD, List(XTypeTag.tpe, YTypeTag.tpe), List(XTypeTag.tpe))
}

class DOperationsCatalogSuite extends FunSuite with Matchers {

  test("It is possible to create instance of registered DOperation") {
    import DOperationCatalogTestResources._
    val catalog = DOperationsCatalog()
    val name = "name"
    catalog.registerDOperation[DOperationA](name, CategoryTree.ML.Regression, "")
    val instance = catalog.createDOperation(name)
    assert(instance == DOperationA())
  }

  test("Attempt of creating unregistered DOperation raises exception") {
    intercept[DOperationNotFoundException] {
      val catalog = DOperationsCatalog()
      catalog.createDOperation("unknown DOperation name")
    }
  }

  test("Registering DOperation without parameterless constructor raises exception") {
    intercept[NoParameterlessConstructorInDOperationException] {
      import DOperationCatalogTestResources._
      val catalog = DOperationsCatalog()
      catalog.registerDOperation[DOperationWithoutParameterlessConstructor](
        "name", CategoryTree.ML.Regression, "description")
    }
  }

  test("It is possible to view list of registered DOperations descriptors") {
    import ViewingTestResources._
    assert(catalog.operations == Set(expectedA, expectedB, expectedC, expectedD))
  }

  test("It is possible to get tree of registered categories and DOperations") {
    import DOperationCatalogTestResources.CategoryTree._
    import ViewingTestResources._

    val root: DOperationCategoryNode = catalog.categoryTree
    root.operations should contain theSameElementsAs Seq.empty
    root.successors.keys should contain theSameElementsAs Seq(ML)

    val mlNode = root.successors(ML)
    mlNode.operations should contain theSameElementsAs Seq(expectedD)
    mlNode.successors.keys should contain theSameElementsAs Seq(ML.Regression, ML.Classification)

    val regressionNode = mlNode.successors(ML.Regression)
    regressionNode.operations should contain theSameElementsAs Seq(expectedA, expectedB)
    regressionNode.successors.keys should contain theSameElementsAs Seq()

    val classificationNode = mlNode.successors(ML.Classification)
    classificationNode.operations should contain theSameElementsAs Seq(expectedC)
    classificationNode.successors.keys should contain theSameElementsAs Seq()
  }
}
