/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.dataframe

import org.apache.spark.sql

import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.parameters.{IndexSingleColumnSelection, NameSingleColumnSelection, SingleColumnSelection}

/*
* @param optionalSparkDataFrame spark representation of data.
*                               Client of this class has to assure that
*                               sparkDataFrame data fulfills its internal schema.
*/
class DataFrame(optionalSparkDataFrame: Option[sql.DataFrame] = None) extends DOperable {

  def this() = this(None)

  def sparkDataFrame: sql.DataFrame = optionalSparkDataFrame.get

  def save(path: String): Unit = {
    sparkDataFrame.toJSON.saveAsTextFile(path)
  }

  def getColumnName(singleColumnSelection: SingleColumnSelection): String = {
    singleColumnSelection match {
      case NameSingleColumnSelection(value) => value
      case IndexSingleColumnSelection(index) => sparkDataFrame.schema(index).name
    }
  }

}
