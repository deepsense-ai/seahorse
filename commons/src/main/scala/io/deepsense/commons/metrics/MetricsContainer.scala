/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.metrics

import java.io.File
import java.util.concurrent.TimeUnit

import com.codahale.metrics.{MetricRegistry, CsvReporter}
import nl.grons.metrics.scala.InstrumentedBuilder

object MetricsContainer {

  val reportPeriod = 10
  val metricRegistry = new MetricRegistry()

  CsvReporter.forRegistry(metricRegistry)
    .convertRatesTo(TimeUnit.SECONDS)
    .convertDurationsTo(TimeUnit.MILLISECONDS)
    .build(new File("/var/log/deepsense/metrics"))
    .start(reportPeriod, TimeUnit.SECONDS)
}

trait Instrumented extends InstrumentedBuilder {
  val metricRegistry = MetricsContainer.metricRegistry
}
