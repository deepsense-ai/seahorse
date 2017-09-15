/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.experimentmanager

import java.util.UUID

import io.deepsense.deeplang.catalogs.doperations.DOperationCategory

object DOperationCategories {

  // TODO hard-code all ids

  object IO extends DOperationCategory(UUID.randomUUID(), "Input/Output")

  object DataManipulation
    extends DOperationCategory(UUID.randomUUID(), "Data manipulation")

  object ML extends DOperationCategory(UUID.randomUUID(), "Machine learning") {

    object Regression extends DOperationCategory(UUID.randomUUID(), "Regression", ML)

    object Classification extends DOperationCategory(UUID.randomUUID(), "Classification", ML)

    object Clustering extends DOperationCategory(UUID.randomUUID(), "Clustering", ML)

    object Evaluation extends DOperationCategory(UUID.randomUUID(), "Evaluation", ML)

    object FeatureSelection extends DOperationCategory(UUID.randomUUID(), "Feature selection", ML)
  }

  object Utils extends DOperationCategory(UUID.randomUUID(), "Utilities", None)
}
