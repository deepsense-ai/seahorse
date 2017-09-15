/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage.factories

import scala.util.Random

import io.deepsense.models.entities.{DataObjectReference, DataObjectReport}

trait DataObjectTestFactory {

  private val random = new Random()

  def testDataObjectReport: DataObjectReport =
    DataObjectReport(new String(random.alphanumeric.take(32).toArray))

  def testDataObjectReference: DataObjectReference = DataObjectReference("hdfs://whatever")
}
