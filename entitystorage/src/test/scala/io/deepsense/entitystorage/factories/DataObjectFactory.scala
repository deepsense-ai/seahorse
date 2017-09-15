/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage.factories

import java.util.UUID

import io.deepsense.models.entities.{DataObjectReference, DataObjectReport}

trait DataObjectFactory {

  def testDataObjectReport: DataObjectReport =
    DataObjectReport(UUID.randomUUID().toString)

  def testDataObjectReference: DataObjectReference = DataObjectReference("hdfs://whatever")
}
