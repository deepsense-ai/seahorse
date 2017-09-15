/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.dataframe

import org.apache.spark.sql

import io.deepsense.deeplang.DOperable

/**
 * @param optionalSparkDataFrame spark representation of data. Client of this class has to assure that
 *                       sparkDataFrame data fulfills its internal schema.
 */
class DataFrame(optionalSparkDataFrame: Option[sql.DataFrame] = None) extends DOperable {

  def this() = this(None)

  def sparkDataFrame: sql.DataFrame = optionalSparkDataFrame.get

}
