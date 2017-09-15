/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.models.workflows

import io.deepsense.graph.Graph

/**
 * The common part of e.g. Experiment and InputExperiment models.
 * @param name Name of the experiment.
 * @param description Description of the experiment.
 * @param graph Experiment's graph. Never null but can be empty.
 */
@SerialVersionUID(1)
abstract class BaseWorkflow(name: String, description: String, graph: Graph) extends Serializable
