/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.ColumnRole.ColumnRole
import io.deepsense.deeplang.parameters.ColumnType.ColumnType

case class BooleanParameter(value: Boolean) extends Parameter

case class NumericParameter(value: Double) extends Parameter

case class StringParameter(value: String) extends Parameter

case class ChoiceParameter(label: String, value: ParametersSchema) extends Parameter

case class MultipleChoiceParameter(values: Set[ChoiceParameter]) extends Parameter

case class MultiplicatorParameter(value: List[ParametersSchema]) extends Parameter

abstract class SingleColumnSelection(typeName: String) extends Parameter

case class IndexSingleColumnSelection(value: Int) extends SingleColumnSelection("index")

case class NameSingleColumnSelection(value: String) extends SingleColumnSelection("column")

abstract class ColumnSelection(typeName: String)

case class NameColumnSelection(values: List[String]) extends ColumnSelection("columnList")

case class IndexColumnSelection(values: List[Int]) extends ColumnSelection("indexList")

case class RoleColumnSelection(values: List[ColumnRole]) extends ColumnSelection("roleList")

case class TypeColumnSelection(values: List[ColumnType]) extends ColumnSelection("typeList")

case class MultipleColumnSelection(values: List[ColumnSelection]) extends Parameter
