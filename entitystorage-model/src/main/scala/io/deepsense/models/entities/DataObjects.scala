/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.models.entities

/**
 * Report representation with all its content.
 */
case class DataObjectReport(jsonReport: String)

/**
 * Reference to an actual data e.g. DataFrame, Model, etc
 */
case class DataObjectReference(url: String)
