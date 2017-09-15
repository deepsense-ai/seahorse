/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.ColumnRole._
import io.deepsense.deeplang.parameters.ColumnType._

/**
 * Represents selecting subset of columns of dataframe.
 */
sealed abstract class ColumnSelection(typeName: String)

/**
 * Represents selecting subset of columns which have one of given names.
 */
case class NameColumnSelection(names: List[String]) extends ColumnSelection("columnList")

/**
 * Represents selecting subset of columns which have one of given indexes.
 */
case class IndexColumnSelection(indexes: List[Int]) extends ColumnSelection("indexList")

/**
 * Represents selecting subset of columns which have one of given roles.
 */
case class RoleColumnSelection(roles: List[ColumnRole]) extends ColumnSelection("roleList")

/**
 * Represents selecting subset of columns which have one of given types.
 */
case class TypeColumnSelection(types: List[ColumnType]) extends ColumnSelection("typeList")
