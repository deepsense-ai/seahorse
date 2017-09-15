/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.ColumnRole.ColumnRole
import io.deepsense.deeplang.parameters.ColumnType.ColumnType

case class ChoiceParameter(label: String, chosenSchema: ParametersSchema)

case class MultipleChoiceParameter(choices: Traversable[ChoiceParameter])

case class MultiplicatorParameter(schemas: List[ParametersSchema])


abstract class SingleColumnSelection(typeName: String)

case class IndexSingleColumnSelection(value: Int) extends SingleColumnSelection("index")

case class NameSingleColumnSelection(value: String) extends SingleColumnSelection("column")


case class MultipleColumnSelection(selections: List[ColumnSelection])

abstract class ColumnSelection(typeName: String)

case class NameColumnSelection(names: List[String]) extends ColumnSelection("columnList")

case class IndexColumnSelection(indexes: List[Int]) extends ColumnSelection("indexList")

case class RoleColumnSelection(roles: List[ColumnRole]) extends ColumnSelection("roleList")

case class TypeColumnSelection(types: List[ColumnType]) extends ColumnSelection("typeList")

