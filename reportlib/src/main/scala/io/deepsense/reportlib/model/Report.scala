/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model

case class Report(tables: Map[String, Table], distributions: Map[String, Distribution])
