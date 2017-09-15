/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.factories

import java.util.UUID

import io.deepsense.deeplang.doperables.Report
import io.deepsense.models.entities.{DataObjectReference, DataObjectReport}

trait DataObjectFactory {

  def testDataObjectReport: DataObjectReport =
    DataObjectReport(Report(UUID.randomUUID().toString))

  def testDataObjectReference: DataObjectReference = DataObjectReference("hdfs://whatever")
}
