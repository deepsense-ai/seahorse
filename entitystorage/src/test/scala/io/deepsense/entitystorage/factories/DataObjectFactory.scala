/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.factories

import io.deepsense.deeplang.doperables.Report
import io.deepsense.entitystorage.models.{DataObjectReference, DataObjectReport}

trait DataObjectFactory {

  def testDataObjectReport: DataObjectReport = DataObjectReport(Report())

  def testDataObjectReference: DataObjectReference = DataObjectReference("hdfs://whatever")
}
