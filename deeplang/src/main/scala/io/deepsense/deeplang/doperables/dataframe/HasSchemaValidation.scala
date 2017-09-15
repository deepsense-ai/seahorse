/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import scala.collection.mutable

import org.apache.spark.sql.types._

trait HasSchemaValidation {

  def validateSchema(schema: StructType): Unit = {
    validateColumnNameUniqueness(schema)
    validateIdColumn(schema)
  }

  /**
   * Checks if there is at most one id column with type int or string.
   */
  private[this] def validateIdColumn(schema: StructType): Unit = {
    val ids = schema.filter(isIdColumn)
    require(ids.size <= 1,
      s"Multiple columns with id role. ${ids.map(_.name).mkString("[", ",", "]")}")
    val idColumnType = ids.headOption.map(_.dataType)
    val correctType = idColumnType.forall(dt => dt == StringType || dt == IntegerType)
    if (!correctType) {
      throw new IllegalArgumentException(s"Column ${ids.head.name} with id role has invalid type:"
        + s"${idColumnType.get}. Allowed types: $IntegerType, $StringType")
    }
  }

  private[this]def isIdColumn(column: StructField): Boolean = {
    column.metadata.contains("role") && ColumnRole.Id.name == column.metadata.getString("role")
  }

  private[this] def validateColumnNameUniqueness(schema: StructType): Unit = {
    val names: mutable.Set[String] = mutable.Set[String]()
    schema.foreach(f =>
      if (!names.contains(f.name)) {
        names += f.name
      } else {
        throw new IllegalArgumentException(s"Schema column names uniqueness violated: ${f.name}.")
      }
    )
  }

}
