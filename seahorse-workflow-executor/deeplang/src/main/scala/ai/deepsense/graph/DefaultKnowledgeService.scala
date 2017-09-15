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

package ai.deepsense.graph

import scala.reflect.runtime.{universe => ru}

import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.{DKnowledge, DOperable, DOperation}

object DefaultKnowledgeService {

  /**
    * @return Knowledge vector for output ports if no additional information is provided.
    */
  def defaultOutputKnowledge(
      catalog: DOperableCatalog,
      operation: DOperation): Vector[DKnowledge[DOperable]] =
    for (outPortType <- operation.outPortTypes) yield defaultKnowledge(catalog, outPortType)

  /**
    * @return Knowledge vector for input ports if no additional information is provided.
    */
  def defaultInputKnowledge(
      catalog: DOperableCatalog,
      operation: DOperation): Vector[DKnowledge[DOperable]] =
    for (inPortType <- operation.inPortTypes) yield defaultKnowledge(catalog, inPortType)

  /**
    * @return Knowledge for port if no additional information is provided.
    */
  def defaultKnowledge(
      catalog: DOperableCatalog,
      portType: ru.TypeTag[_]): DKnowledge[DOperable] = {
    val castedType = portType.asInstanceOf[ru.TypeTag[DOperable]]
    DKnowledge(catalog.concreteSubclassesInstances(castedType))
  }
}
