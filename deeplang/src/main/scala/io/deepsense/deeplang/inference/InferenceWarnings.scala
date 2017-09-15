/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.inference

/**
 * Container for inference warnings.
 */
case class InferenceWarnings(warnings: Vector[InferenceWarning]) {
  def :+(warning: InferenceWarning): InferenceWarnings =
    InferenceWarnings(warnings :+ warning)

  def ++(other: InferenceWarnings): InferenceWarnings =
    InferenceWarnings(warnings ++ other.warnings)
}

object InferenceWarnings {
  def empty: InferenceWarnings = InferenceWarnings(Vector.empty[InferenceWarning])

  def apply(warnings: InferenceWarning*): InferenceWarnings = InferenceWarnings(warnings.toVector)
}
