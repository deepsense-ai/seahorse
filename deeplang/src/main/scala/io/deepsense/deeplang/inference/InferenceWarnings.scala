/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.inference

import io.deepsense.deeplang.doperables.dataframe.DataFrameMetadata
import io.deepsense.deeplang.parameters.{SingleColumnSelection, ColumnSelection}

/**
 * Container for inference warnings.
 */
case class InferenceWarnings(warnings: Vector[InferenceWarning]) {
  def :+(warning: InferenceWarning): InferenceWarnings =
    InferenceWarnings(warnings :+ warning)

  def ++(other: InferenceWarnings): InferenceWarnings =
    InferenceWarnings(warnings ++ other.warnings)

  def isEmpty(): Boolean = warnings.isEmpty
}

object InferenceWarnings {
  def empty: InferenceWarnings = InferenceWarnings(Vector.empty[InferenceWarning])

  def apply(warnings: InferenceWarning*): InferenceWarnings = InferenceWarnings(warnings.toVector)

  def flatten(inferenceWarnings: Vector[InferenceWarnings]): InferenceWarnings =
    InferenceWarnings(inferenceWarnings.map(_.warnings).flatten)
}
