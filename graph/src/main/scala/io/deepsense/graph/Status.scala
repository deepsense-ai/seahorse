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

package io.deepsense.graph

object Status extends Enumeration {
  // TODO: Merge with Experiment.Status if appropriate https://codilime.atlassian.net/browse/DS-775
  type Status = Value
  val Draft = Value(0, "DRAFT")
  val Queued = Value(1, "QUEUED")
  val Running = Value(2, "RUNNING")
  val Completed = Value(3, "COMPLETED")
  val Failed = Value(4, "FAILED")
  val Aborted = Value(5, "ABORTED")
}
