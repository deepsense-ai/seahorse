/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.models.entities

/**
 * Report representation with all its content.
 */
case class DataObjectReport(jsonReport: String)

/**
 * Holds path to saved data and related metadata
 * @param savedDataPath path to the data
 * @param metadata metadata serialized as json
 */
case class DataObjectReference(
    savedDataPath: String,
    metadata: String)
