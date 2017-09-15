/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.workflowexecutor.executor

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.ReportLevel._

/**
 * SessionExecutor waits for user instructions in an infinite loop.
 */
case class SessionExecutor(reportLevel: ReportLevel) extends Executor {

  /**
   * WARNING: Performs an infinite loop.
   */
  def execute(): Unit = {
    logger.debug("SessionExecutor starts")
    val executionContext = createExecutionContext(reportLevel)

    // TODO: some kind of infinite loop / some kind of break condition
    // also: You might want to move something from WorkflowExecutor class to Executor trait
    // to make it usable from here.
    var counter = 0
    while(true) {
      counter = counter + 1
      logger.debug("Loop lap number: " + counter)
      Thread.sleep(10 * 1000)
    }

    cleanup(executionContext)
    logger.debug("SessionExecutor ends")
  }

  private def cleanup(executionContext: ExecutionContext): Unit = {
    logger.debug("Cleaning up...")
    executionContext.sparkContext.stop()
    logger.debug("Spark terminated!")
  }
}
