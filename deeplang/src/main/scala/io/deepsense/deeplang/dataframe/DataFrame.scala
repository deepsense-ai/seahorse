/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.dataframe

import org.apache.spark.sql

import io.deepsense.deeplang.DOperable

/**
 * @param sparkDataFrame spark representation of data. Client of this class has to assure that
 *                       sparkDataFrame data fulfills its internal schema.
 */
case class DataFrame private[dataframe] (val sparkDataFrame: sql.DataFrame)
  extends DOperable
