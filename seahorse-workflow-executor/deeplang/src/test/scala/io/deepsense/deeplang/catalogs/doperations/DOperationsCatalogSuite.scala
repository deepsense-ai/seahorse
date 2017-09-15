/**
 * Copyright 2015, deepsense.ai
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

package io.deepsense.deeplang.catalogs.doperations


import scala.reflect.runtime.universe.{TypeTag, typeTag}

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperations.exceptions._
import io.deepsense.deeplang.doperables.DOperableMock
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}

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

  abstract class DOperationMock extends DOperation {

    override def inPortTypes: Vector[TypeTag[_]] = Vector()

    override def outPortTypes: Vector[TypeTag[_]] = Vector()

    override def inferKnowledgeUntyped(
        l: Vector[DKnowledge[DOperable]])(
        context: InferContext): (Vector[DKnowledge[DOperable]], InferenceWarnings) = ???

    override def executeUntyped(l: Vector[DOperable])(context: ExecutionContext): Vector[DOperable] = ???

    override val inArity: Int = 2
    override val outArity: Int = 3
    val params: Array[io.deepsense.deeplang.params.Param[_]] = Array()
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

  val descriptionA = "descriptionA"
  val descriptionB = "descriptionB"
  val descriptionC = "descriptionC"
  val descriptionD = "descriptionD"

  val versionA = "versionA"
  val versionB = "versionB"
  val versionC = "versionC"
  val versionD = "versionD"

  case class DOperationA() extends DOperationMock {
    override val id = idA
    override val name = nameA
    override val description = descriptionA
  }

  case class DOperationB() extends DOperationMock {
    override val id = idB
    override val name = nameB
    override val description = descriptionB
  }

  case class DOperationC() extends DOperationMock {
    override val id = idC
    override val name = nameC
    override val description = descriptionC
  }

  case class DOperationD() extends DOperationMock {
    override val id = idD
    override val name = nameD
    override val description = descriptionD
    override val inPortTypes: Vector[TypeTag[_]] = Vector(XTypeTag, YTypeTag)
    override val outPortTypes: Vector[TypeTag[_]] = Vector(XTypeTag)
  }

}

object ViewingTestResources extends MockitoSugar {
  import DOperationCatalogTestResources._

  val categoryA = CategoryTree.ML.Regression
  val categoryB = CategoryTree.ML.Regression
  val categoryC = CategoryTree.ML.Classification
  val categoryD = CategoryTree.ML

  val catalog = DOperationsCatalog()

  catalog.registerDOperation(categoryA, () => new DOperationA())
  catalog.registerDOperation(categoryB, () => new DOperationB())
  catalog.registerDOperation(categoryC, () => new DOperationC())
  catalog.registerDOperation(categoryD, () => new DOperationD())

  val operationD = catalog.createDOperation(idD)

  val expectedA = DOperationDescriptor(idA, nameA, descriptionA, categoryA, hasDocumentation = false,
    DOperationA().paramsToJson, Nil, Vector.empty, Nil, Vector.empty)
  val expectedB = DOperationDescriptor(idB, nameB, descriptionB, categoryB, hasDocumentation = false,
    DOperationB().paramsToJson, Nil, Vector.empty, Nil, Vector.empty)
  val expectedC = DOperationDescriptor(idC, nameC, descriptionC, categoryC, hasDocumentation = false,
    DOperationC().paramsToJson, Nil, Vector.empty, Nil, Vector.empty)
  val expectedD = DOperationDescriptor(idD, nameD, descriptionD, categoryD, hasDocumentation = false,
    DOperationD().paramsToJson, List(XTypeTag.tpe, YTypeTag.tpe), operationD.inPortsLayout,
    List(XTypeTag.tpe), operationD.outPortsLayout)
}

class DOperationsCatalogSuite extends FunSuite with Matchers with MockitoSugar {

  test("It is possible to create instance of registered DOperation") {
    import DOperationCatalogTestResources._
    val catalog = DOperationsCatalog()
    catalog.registerDOperation(CategoryTree.ML.Regression, () => new DOperationA())
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
    mlNode.operations shouldBe List(expectedD)
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
