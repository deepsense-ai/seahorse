/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.catalogs.doperations.exceptions

import io.deepsense.deeplang.DOperation

case class DOperationNotFoundException(operationId: DOperation.Id)
  extends DOperationsCatalogException(s"DOperation not found: $operationId")
