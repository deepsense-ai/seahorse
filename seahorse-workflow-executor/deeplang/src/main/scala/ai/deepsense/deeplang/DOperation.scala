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
import java.util.UUID

import ai.deepsense.commons.models
import ai.deepsense.commons.utils.{CollectionExtensions, Logging}
import ai.deepsense.deeplang.DOperation.{ReportParam, ReportType}
import ai.deepsense.deeplang.DPortPosition.DPortPosition
import ai.deepsense.deeplang.catalogs.doperations.DOperationCategory
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.{Param, Params}
import ai.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import ai.deepsense.graph.{GraphKnowledge, Operation}

/**
 * DOperation that receives and returns instances of DOperable.
 * Can infer its output type based on type knowledge.
 */
@SerialVersionUID(1L)
abstract class DOperation extends Operation
    with Serializable with Logging with Params {
  import CollectionExtensions._

  val inArity: Int
  val outArity: Int
  val id: DOperation.Id
  val name: String
  val description: String
  def specificParams: Array[Param[_]]
  def reportTypeParam: Option[Param[_]] = Option(reportType).filter(_ => outArity != 0)
  def params: Array[Param[_]] = specificParams ++ reportTypeParam

  def hasDocumentation: Boolean = false

  def inPortTypes: Vector[ru.TypeTag[_]]

  def inPortsLayout: Vector[DPortPosition] = defaultPortLayout(inPortTypes, GravitateLeft)

  def outPortTypes: Vector[ru.TypeTag[_]]

  def outPortsLayout: Vector[DPortPosition] = defaultPortLayout(outPortTypes, GravitateRight)

  def getDatasourcesIds: Set[UUID] = Set[UUID]()

  private def defaultPortLayout(portTypes: Vector[ru.TypeTag[_]], gravity: Gravity): Vector[DPortPosition] = {
    import DPortPosition._
    portTypes.size match {
      case 0 => Vector.empty
      case 1 => Vector(Center)
      case 2 => gravity match {
        case GravitateLeft => Vector(Left, Center)
        case GravitateRight => Vector(Center, Right)
      }
      case 3 => Vector(Left, Center, Right)
      case other => throw new IllegalStateException(s"Unsupported number of output ports: $other")
    }
  }

  def validate(): Unit = {
    require(outPortsLayout.size == outPortTypes.size, "Every output port must be laid out")
    require(!outPortsLayout.hasDuplicates, "Output port positions must be unique")
    require(inPortsLayout.size == inPortTypes.size, "Every input port must be laid out")
    require(!inPortsLayout.hasDuplicates, "Input port positions must be unique")
    require(inPortsLayout.isSorted, "Input ports must be laid out from left to right")
    require(outPortsLayout.isSorted, "Output ports must be laid out from left to right")
  }

  def executeUntyped(l: Vector[DOperable])(context: ExecutionContext): Vector[DOperable]

  /**
   * Infers knowledge for this operation.
   *
   * @param context Infer context to be used in inference.
   * @param inputKnowledge Vector of knowledge objects to be put in input ports of this operation.
   *                       This method assumes that size of this vector is equal to [[inArity]].
   * @return A tuple consisting of:
   *          - vector of knowledge object for each of operation's output port
   *          - inference warnings for this operation
   */
  def inferKnowledgeUntyped(
      inputKnowledge: Vector[DKnowledge[DOperable]])(
      context: InferContext)
      : (Vector[DKnowledge[DOperable]], InferenceWarnings)

  def inferGraphKnowledgeForInnerWorkflow(context: InferContext): GraphKnowledge = GraphKnowledge()

  def typeTag[T : ru.TypeTag]: ru.TypeTag[T] = ru.typeTag[T]

  val reportType: ChoiceParam[ReportType] = ChoiceParam[ReportType](
    name = "report type",
    description = Some("Output entities report type. Computing extended report can be time consuming."))
  setDefault(reportType, ReportParam.Extended())

  def getReportType: ReportType = $(reportType)

  def setReportType(value: ReportType): this.type = set(reportType, value)
}

object DOperation {
  type Id = models.Id
  val Id = models.Id

  object ReportParam {
    case class Metadata() extends ReportType {
      override val name: String = "Metadata report"
      override val params: Array[Param[_]] = Array()
    }

    case class Extended() extends ReportType {
      override val name: String = "Extended report"
      override val params: Array[Param[_]] = Array()
    }
  }

  sealed trait ReportType extends Choice {
    import ai.deepsense.deeplang.DOperation.ReportParam.{Extended, Metadata}
    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Metadata],
      classOf[Extended])
  }
}
