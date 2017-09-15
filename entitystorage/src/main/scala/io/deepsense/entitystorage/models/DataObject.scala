/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.models

import io.deepsense.deeplang.doperables.Report

/**
 * Superclass represents data stored in Entity.
 * DataObject can be a Report or a reference containing hdfs path do actual data
 */
abstract class DataObject

/**
 * Report representation with all its content.
 */
case class DataObjectReport(report: Report) extends DataObject

/**
 * Reference to an actual data ex. DataFrame, Model, etc
 */
case class DataObjectReference(url: String) extends DataObject
