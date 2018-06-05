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

package ai.deepsense.deeplang.catalogs.doperations


import scala.reflect.runtime.universe.{TypeTag, typeTag}

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}
import ai.deepsense.deeplang._
import ai.deepsense.deeplang.catalogs.SortPriority
import ai.deepsense.deeplang.catalogs.doperations.exceptions._
import ai.deepsense.deeplang.doperables.DOperableMock
import ai.deepsense.deeplang.doperations.UnknownOperation
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}

object DOperationCatalogTestResources {
  object CategoryTree {
    object IO extends DOperationCategory(DOperationCategory.Id.randomId, "Input/Output", SortPriority.coreDefault)

    object DataManipulation
      extends DOperationCategory(DOperationCategory.Id.randomId, "Data manipulation", IO.priority.nextCore)

    object ML extends DOperationCategory(
      DOperationCategory.Id.randomId, "Machine learning", DataManipulation.priority.nextCore) {

      object Classification
        extends DOperationCategory(DOperationCategory.Id.randomId, "Classification", ML.priority.nextCore, ML)

      object Regression
        extends DOperationCategory(DOperationCategory.Id.randomId, "Regression", Classification.priority.nextCore, ML)


      object Clustering
        extends DOperationCategory(DOperationCategory.Id.randomId, "Clustering", Regression.priority.nextCore, ML)

      object Evaluation
        extends DOperationCategory(DOperationCategory.Id.randomId, "Evaluation", Clustering.priority.nextCore, ML)
    }

    object Utils
      extends DOperationCategory(DOperationCategory.Id.randomId, "Utilities", ML.Evaluation.priority.nextCore)
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
    val specificParams: Array[ai.deepsense.deeplang.params.Param[_]] = Array()
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

  val priorityA = SortPriority(0)
  val priorityB = SortPriority(-1)
  val priorityC = SortPriority(2)
  val priorityD = SortPriority(3)
  catalog.registerDOperation(categoryA, () => new DOperationA(), priorityA)
  catalog.registerDOperation(categoryB, () => new DOperationB(), priorityB)
  catalog.registerDOperation(categoryC, () => new DOperationC(), priorityC)
  catalog.registerDOperation(categoryD, () => new DOperationD(), priorityD)

  val operationD = catalog.createDOperation(idD)

  val expectedA = DOperationDescriptor(idA, nameA, descriptionA, categoryA, priorityA, hasDocumentation = false,
    DOperationA().paramsToJson, Nil, Vector.empty, Nil, Vector.empty)
  val expectedB = DOperationDescriptor(idB, nameB, descriptionB, categoryB, priorityB, hasDocumentation = false,
    DOperationB().paramsToJson, Nil, Vector.empty, Nil, Vector.empty)
  val expectedC = DOperationDescriptor(idC, nameC, descriptionC, categoryC, priorityC, hasDocumentation = false,
    DOperationC().paramsToJson, Nil, Vector.empty, Nil, Vector.empty)
  val expectedD = DOperationDescriptor(idD, nameD, descriptionD, categoryD, priorityD, hasDocumentation = false,
    DOperationD().paramsToJson, List(XTypeTag.tpe, YTypeTag.tpe), operationD.inPortsLayout,
    List(XTypeTag.tpe), operationD.outPortsLayout)
}

class DOperationsCatalogSuite extends FunSuite with Matchers with MockitoSugar {

  test("It is possible to create instance of registered DOperation") {
    import DOperationCatalogTestResources._
    val catalog = DOperationsCatalog()
    catalog.registerDOperation(CategoryTree.ML.Regression, () => new DOperationA(), ViewingTestResources.priorityA)
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

    val exceptUnknown = catalog.operations - new UnknownOperation().id

    exceptUnknown shouldBe Map(
      idA -> expectedA,
      idB -> expectedB,
      idC -> expectedC,
      idD -> expectedD)
  }

  test("SortPriority inSequence assigns values with step 100") {
    import ai.deepsense.deeplang.catalogs.doperations.DOperationCatalogTestResources.CategoryTree
    CategoryTree.ML.Classification.priority shouldBe SortPriority(300)
    CategoryTree.ML.Regression.priority shouldBe SortPriority(400)
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
    mlNode.successors.keys.toList shouldBe List(ML.Classification, ML.Regression)

    val regressionNode = mlNode.successors(ML.Regression)
    regressionNode.category shouldBe Some(ML.Regression)
    regressionNode.operations shouldBe List(expectedB, expectedA)
    regressionNode.successors.keys shouldBe empty

    val classificationNode = mlNode.successors(ML.Classification)
    classificationNode.category shouldBe Some(ML.Classification)
    classificationNode.operations should contain theSameElementsAs Seq(expectedC)
    classificationNode.successors.keys shouldBe empty
  }
}
