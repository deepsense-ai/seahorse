/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.catalogs.doperations


import scala.reflect.runtime.universe.{TypeTag, typeTag}

import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperations.exceptions._
import io.deepsense.deeplang.doperables.DOperableMock
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.parameters.ParametersSchema

object DOperationCatalogTestResources {
  object CategoryTree {
    object IO extends DOperationCategory(DOperationCategory.Id.randomId, "Input/Output")

    object DataManipulation
      extends DOperationCategory(DOperationCategory.Id.randomId, "Data manipulation")

    object ML extends DOperationCategory(DOperationCategory.Id.randomId, "Machine learning") {

      object Regression extends DOperationCategory(DOperationCategory.Id.randomId, "Regression", ML)

      object Classification
        extends DOperationCategory(DOperationCategory.Id.randomId, "Classification", ML)

      object Clustering extends DOperationCategory(DOperationCategory.Id.randomId, "Clustering", ML)

      object Evaluation extends DOperationCategory(DOperationCategory.Id.randomId, "Evaluation", ML)
    }

    object Utils extends DOperationCategory(DOperationCategory.Id.randomId, "Utilities", None)
  }

  val parametersSchema = ParametersSchema()

  abstract class DOperationMock extends DOperation {
    def inPortTypes: Vector[TypeTag[_]] = Vector()

    def outPortTypes: Vector[TypeTag[_]] = Vector()

    def inferKnowledge(
        context: InferContext)(
        l: Vector[DKnowledge[DOperable]]): (Vector[DKnowledge[DOperable]], InferenceWarnings) = ???

    def execute(context: ExecutionContext)(l: Vector[DOperable]): Vector[DOperable] = ???

    override val inArity: Int = 2
    override val outArity: Int = 3
    override val parameters = parametersSchema
  }

  case class X() extends DOperableMock
  case class Y() extends DOperableMock

  val XTypeTag = typeTag[X]
  val YTypeTag = typeTag[Y]

  val idA = DOperation.Id.randomId
  val idB = DOperation.Id.randomId
  val idC = DOperation.Id.randomId
  val idD = DOperation.Id.randomId

  val nameA = "nameA"
  val nameB = "nameB"
  val nameC = "nameC"
  val nameD = "nameD"

  val versionA = "versionA"
  val versionB = "versionB"
  val versionC = "versionC"
  val versionD = "versionD"

  case class DOperationA() extends DOperationMock {
    override val id = idA
    override val name = nameA
    override val version = versionA
  }

  case class DOperationB() extends DOperationMock {
    override val id = idB
    override val name = nameB
    override val version = versionB
  }

  case class DOperationC() extends DOperationMock {
    override val id = idC
    override val name = nameC
    override val version = versionC
  }

  case class DOperationD() extends DOperationMock {
    override val id = idD
    override val name = nameD
    override val version = versionD
    override val inPortTypes: Vector[TypeTag[_]] = Vector(XTypeTag, YTypeTag)
    override val outPortTypes: Vector[TypeTag[_]] = Vector(XTypeTag)
  }

  case class DOperationWithoutParameterlessConstructor(x: Int) extends DOperationMock {
    override val id = DOperation.Id.randomId
    override val name = "some name"
    override val inArity: Int = 2
    override val outArity: Int = 3
  }
}

object ViewingTestResources extends MockitoSugar {
  import DOperationCatalogTestResources._

  val descriptionA = "descriptionA"
  val descriptionB = "descriptionB"
  val descriptionC = "descriptionC"
  val descriptionD = "descriptionD"

  val categoryA = CategoryTree.ML.Regression
  val categoryB = CategoryTree.ML.Regression
  val categoryC = CategoryTree.ML.Classification
  val categoryD = CategoryTree.ML

  val catalog = DOperationsCatalog()

  catalog.registerDOperation[DOperationA](categoryA, descriptionA)
  catalog.registerDOperation[DOperationB](categoryB, descriptionB)
  catalog.registerDOperation[DOperationC](categoryC, descriptionC)
  catalog.registerDOperation[DOperationD](categoryD, descriptionD)

  val expectedA = DOperationDescriptor(
    idA, nameA, versionA, descriptionA, categoryA, parametersSchema, Nil, Nil)
  val expectedB = DOperationDescriptor(
    idB, nameB, versionB, descriptionB, categoryB, parametersSchema, Nil, Nil)
  val expectedC = DOperationDescriptor(
    idC, nameC, versionC, descriptionC, categoryC, parametersSchema, Nil, Nil)
  val expectedD = DOperationDescriptor(
    idD, nameD, versionD, descriptionD, categoryD, parametersSchema,
    List(XTypeTag.tpe, YTypeTag.tpe), List(XTypeTag.tpe))
}

class DOperationsCatalogSuite extends FunSuite with Matchers with MockitoSugar {

  test("It is possible to create instance of registered DOperation") {
    import DOperationCatalogTestResources._
    val catalog = DOperationsCatalog()
    catalog.registerDOperation[DOperationA](CategoryTree.ML.Regression, "")
    val instance = catalog.createDOperation(idA)
    assert(instance == DOperationA())
  }

  test("Attempt of creating unregistered DOperation raises exception") {
    val nonExistingOperationId = DOperation.Id.randomId
    val exception = intercept[DOperationNotFoundException] {
      val catalog = DOperationsCatalog()
      catalog.createDOperation(nonExistingOperationId)
    }
    exception.operationId shouldBe nonExistingOperationId
  }

  test("Registering DOperation without parameterless constructor raises exception") {
    a [NoParameterlessConstructorInDOperationException] shouldBe thrownBy {
      import DOperationCatalogTestResources._
      val catalog = DOperationsCatalog()
      catalog.registerDOperation[DOperationWithoutParameterlessConstructor](
        CategoryTree.ML.Regression, "description")
    }
  }

  test("It is possible to view list of registered DOperations descriptors") {
    import DOperationCatalogTestResources._
    import ViewingTestResources._

    catalog.operations shouldBe Map(
      idA -> expectedA,
      idB -> expectedB,
      idC -> expectedC,
      idD -> expectedD)
  }

  test("It is possible to get tree of registered categories and DOperations") {
    import DOperationCatalogTestResources.CategoryTree._
    import ViewingTestResources._

    val root: DOperationCategoryNode = catalog.categoryTree
    root.category shouldBe None
    root.operations shouldBe empty
    root.successors.keys should contain theSameElementsAs Seq(ML)

    val mlNode = root.successors(ML)
    mlNode.category shouldBe Some(ML)
    mlNode.operations shouldBe Set(expectedD)
    mlNode.successors.keys should contain theSameElementsAs Seq(ML.Regression, ML.Classification)

    val regressionNode = mlNode.successors(ML.Regression)
    regressionNode.category shouldBe Some(ML.Regression)
    regressionNode.operations should contain theSameElementsAs Seq(expectedA, expectedB)
    regressionNode.successors.keys shouldBe empty

    val classificationNode = mlNode.successors(ML.Classification)
    classificationNode.category shouldBe Some(ML.Classification)
    classificationNode.operations should contain theSameElementsAs Seq(expectedC)
    classificationNode.successors.keys shouldBe empty
  }
}
