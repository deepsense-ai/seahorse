/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{ChoiceParameter, ParametersSchema}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

/**
 * Mock.
 */
case class SelectImportantFeatures() extends DOperation1To1[DataFrame, DataFrame] {
  override val name: String = "Select important features"
  override val id: Id = "c25e034e-ffac-11e4-a322-1697f925ec7b"

  import io.deepsense.deeplang.doperations.SelectImportantFeatures._

  override val parameters = ParametersSchema(
    MethodKey -> ChoiceParameter(
      description = "Method of selecting features",
      default = Some(MethodLabels.StepAIC),
      required = true,
      options = Map(
        MethodLabels.StepAIC -> ParametersSchema())
    )
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    context.dataFrameBuilder.buildDataFrame(
      dataFrame.sparkDataFrame.select(ColumnsNames.head, ColumnsNames.tail:_*))
  }
}

object SelectImportantFeatures {
  val MethodKey = "method"

  object MethodLabels {
    val StepAIC = "step AIC"
  }

  val ColumnsNames = Seq(
    "rating",
    "review_count",
    "stars",
    "highest_price",
    "lowest_price",
    "deposit",
    "petsTRUE",
    "petsUnknown",
    "smokingTRUE",
    "smokingUnknown",
    "check_in",
    "room_count",
    "meeting_roomsUnknown",
    "parking_detailsUnknown",
    "fitness_facilitiesTRUE",
    "fitness_facilitiesUnknown",
    "accessibilityUnknown",
    "cribsUnknown",
    "twentyfour_hour_front_deskUnknown",
    "Bed_and_BreakfastsTRUE",
    "BusinessTRUE",
    "MotelTRUE",
    "FamilyTRUE",
    "B_BTRUE",
    "log_highest_price1",
    "log_lowest_price1",
    "log_review_count1",
    "dist_SF_LA")
}
