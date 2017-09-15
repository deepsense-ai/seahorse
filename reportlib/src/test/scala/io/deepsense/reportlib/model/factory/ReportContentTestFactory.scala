/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model.factory

import io.deepsense.reportlib.model.ReportContent

trait ReportContentTestFactory {

  def testReport: ReportContent = ReportContent(
    ReportContentTestFactory.reportContentName,
    Map(TableTestFactory.tableName -> TableTestFactory.testEmptyTable),
    Map(ReportContentTestFactory.categoricalDistName ->
      DistributionTestFactory.testCategoricalDistribution(
        ReportContentTestFactory.categoricalDistName),
      ReportContentTestFactory.continuousDistName ->
      DistributionTestFactory.testContinuousDistribution(
        ReportContentTestFactory.continuousDistName)
    )
  )
}

object ReportContentTestFactory extends ReportContentTestFactory {
  val continuousDistName = "continuousDistributionName"
  val categoricalDistName = "categoricalDistributionName"
  val reportContentName = "TestReportContentName"
}
