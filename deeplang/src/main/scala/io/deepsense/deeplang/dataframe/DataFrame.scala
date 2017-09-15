/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.dataframe

import java.util.UUID

import org.apache.spark.sql

import io.deepsense.deeplang.DOperable

/**
 * @param id             DataFrame id.
 * @param sparkDataFrame spark representation of data. Client of this class has to assure that
 *                       sparkDataFrame data fulfills its internal schema.
 */
class DataFrame private[dataframe] (val id: UUID, val sparkDataFrame: sql.DataFrame)
  extends DOperable
