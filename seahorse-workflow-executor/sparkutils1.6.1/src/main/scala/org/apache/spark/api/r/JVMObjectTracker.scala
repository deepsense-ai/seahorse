/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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
// scalastyle:off
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// scalastyle:on
/*
 * Prominent changes from original JVMObjectTracker code
 * - Made JVMObjectTracker thread safe by using TrieMap + Atomic Integer
 * - Add logging to JVMObjectTracker
 * - This works because we are running WorkflowExecutor using:
 *   "java" "-cp" "/opt/docker/we.jar:/opt/spark-2.0.2/conf/ \
 *   "ai.deepsense.workflowexecutor.WorkflowExecutorApp"
 * - JVMObjectTracker from we.jar gets loaded first as "Specification order" on page
 *   http://docs.oracle.com/javase/7/docs/technotes/tools/solaris/classpath.html states
 */
package org.apache.spark.api.r

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.concurrent.TrieMap
import org.slf4j.{Logger, LoggerFactory}
/**
  * Helper singleton that tracks JVM objects returned to R.
  * This is useful for referencing these objects in RPC calls.
  */
private[r] object JVMObjectTracker {
  @transient
  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
  private[this] val objMap = new TrieMap[String, Object]
  private[this] val objCounter = new AtomicInteger(0)

  def getObject(id: String): Object = {
    logger.info(s"Get object at $id")
    objMap(id)
  }

  def get(id: String): Option[Object] = {
    logger.info(s"Get object at $id")
    objMap.get(id)
  }

  def put(obj: Object): String = {
    val objId = objCounter.getAndIncrement.toString
    val objName = obj.getClass.getName
    logger.info(s"Puts $objName at $objId ")
    objMap.put(objId, obj)
    objId
  }

  def remove(id: String): Option[Object] = {
    logger.info(s"Removed $id")
    objMap.remove(id)
  }

}
