/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.dhierarchy.exceptions

case class DOperationNotFoundException(operationName: String)
  extends DHierarchyException("DOperation not found: ${operationName}")
