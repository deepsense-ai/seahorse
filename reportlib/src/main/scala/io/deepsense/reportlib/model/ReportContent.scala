/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model

case class ReportContent(
    name: String,
    tables: Map[String, Table] = Map(),
    distributions: Map[String, Distribution] = Map())
