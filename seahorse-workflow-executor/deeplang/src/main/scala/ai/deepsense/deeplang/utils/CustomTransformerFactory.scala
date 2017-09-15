/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.utils

import spray.json.JsObject

import ai.deepsense.deeplang.InnerWorkflowParser
import ai.deepsense.deeplang.doperables.{CustomTransformer, ParamWithValues}
import ai.deepsense.deeplang.params.custom.{InnerWorkflow, PublicParam}

object CustomTransformerFactory {

  def createCustomTransformer(
      innerWorkflow: InnerWorkflow): CustomTransformer = {
    val selectedParams: Seq[ParamWithValues[_]] =
        innerWorkflow.publicParams.flatMap {
      case PublicParam(nodeId, paramName, publicName) =>
        innerWorkflow.graph.nodes.find(_.id == nodeId)
          .flatMap(node => node.value.params.find(_.name == paramName)
          .map(p => {
            ParamWithValues(
              param = p.replicate(publicName),
              defaultValue = node.value.getDefault(p),
              setValue = node.value.get(p))
          }))
    }
    CustomTransformer(innerWorkflow, selectedParams)
  }
}
