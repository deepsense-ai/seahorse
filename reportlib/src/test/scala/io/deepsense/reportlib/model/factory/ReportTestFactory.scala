/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model.factory

import io.deepsense.reportlib.model.Report

trait ReportTestFactory {

  def testReport: Report = Report(
    Map(TableTestFactory.tableName -> TableTestFactory.testEmptyTable),
    Map(ReportTestFactory.categoricalDistName ->
      DistributionTestFactory.testCategoricalDistribution(ReportTestFactory.categoricalDistName),
      ReportTestFactory.continuousDistName ->
      DistributionTestFactory.testContinuousDistribution(ReportTestFactory.continuousDistName)
    )
  )
}

object ReportTestFactory extends ReportTestFactory {
  val continuousDistName = "continuousDistributionName"
  val categoricalDistName = "categoricalDistributionName"
}
