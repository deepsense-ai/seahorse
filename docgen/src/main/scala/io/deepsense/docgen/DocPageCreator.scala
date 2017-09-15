/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.docgen

import java.io.{File, PrintWriter}

import io.deepsense.commons.BuildInfo
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperations.{EstimatorAsFactory, EstimatorAsOperation, EvaluatorAsFactory, TransformerAsOperation}
import io.deepsense.deeplang.params._
import io.deepsense.deeplang.params.choice.{AbstractChoiceParam, Choice, ChoiceParam, MultipleChoiceParam}

trait DocPageCreator {

  /**
    * @return number of pages created
    */
  def createDocPages(
      sparkOperations: Seq[OperationWithSparkClassName],
      forceUpdate: Boolean): Int = {

    sparkOperations.map { case OperationWithSparkClassName(operation, sparkClassName) =>
      val sparkPageFile = new File(
        "docs/operations/" + DocUtils.underscorize(operation.name) + ".md")
      if(!sparkPageFile.exists() || forceUpdate) {
        createDocPage(sparkPageFile, operation, sparkClassName)
        1
      } else {
        0
      }
    }.sum
  }

  // scalastyle:off println
  private def createDocPage(sparkPageFile: File, operation: DOperation, sparkClassName: String) = {
    val writer = new PrintWriter(sparkPageFile)
    writer.println(header(operation))
    writer.println(description(operation))
    writer.println()
    writer.println(sparkDocLink(sparkClassName))
    writer.println()
    writer.println(sinceSeahorseVersion())
    writer.println()
    writer.println(input(operation))
    writer.println()
    writer.println(output(operation))
    writer.println()
    writer.println(parameters(operation))
    writer.flush()
    writer.close()
    println("Created doc page for " + operation.name)
  }
  // scalastyle:on println

  private def header(operation: DOperation): String = {
    s"""---
       |layout: documentation
       |displayTitle: ${operation.name}
       |title: ${operation.name}
       |description: ${operation.name}
       |usesMathJax: true
       |includeOperationsMenu: true
       |---""".stripMargin
  }

  private def description(operation: DOperation): String = {
    DocUtils.forceDotAtEnd(operation.description)
  }

  private def sparkDocLink(sparkClassName: String) = {
    val url = SparkOperationsDocGenerator.sparkDocPrefix + sparkClassName
    "This operation is ported from Spark ML." +
      " For more details, see: " +
      "<a target=\"_blank\" href=\"" + url + "\">" + sparkClassName + " documentation</a>."
  }

  private def sinceSeahorseVersion(): String = {
    val version = BuildInfo.version.replace("-SNAPSHOT", "")
    s"**Since**: Seahorse $version"
  }

  private def input(operation: DOperation): String = {
    val inputTable = operation match {
      case (t: TransformerAsOperation[_]) =>
        inputOutputTable(Seq(
          ("<code><a href=\"../classes/dataframe.html\">DataFrame</a></code>", "Input DataFrame")
        ))
      case (es: EstimatorAsOperation[_]) =>
        inputOutputTable(Seq(
          ("<code><a href=\"../classes/dataframe.html\">DataFrame</a></code>", "Input DataFrame")
        ))
      case (e: EstimatorAsFactory[_]) =>
        "This operation does not take any input."
      case (ev: EvaluatorAsFactory[_]) =>
        "This operation does not take any input."
    }
    "## Input\n\n" + inputTable
  }

  private def output(operation: DOperation): String = {
    val outputTable = operation match {
      case (t: TransformerAsOperation[_]) =>
        inputOutputTable(Seq(
          ("<code><a href=\"../classes/dataframe.html\">DataFrame</a></code>",
            "Output DataFrame"),
          ("<code><a href=\"../classes/transformer.html\">Transformer</a></code>",
            "Transformer that allows to apply the operation on other DataFrames using " +
            "<a href=\"transform.html\">Transform</a>")
        ))
      case (eso: EstimatorAsOperation[_]) =>
        inputOutputTable(Seq(
          ("<code><a href=\"../classes/dataframe.html\">DataFrame</a></code>",
            "Output DataFrame"),
          ("<code><a href=\"../classes/transformer.html\">Transformer</a></code>",
            "Transformer that allows to apply the operation on other DataFrames using " +
              "<a href=\"transform.html\">Transform</a>")
        ))
      case (e: EstimatorAsFactory[_]) =>
        inputOutputTable(Seq(
          ("<code><a href=\"../classes/estimator.html\">Estimator</a></code>",
            "Estimator that can be used in <a href=\"fit.html\">Fit</a> operation")
        ))
      case (ev: EvaluatorAsFactory[_]) =>
        inputOutputTable(Seq(
          ("<code><a href=\"../classes/evaluator.html\">Evaluator</a></code>",
            "Evaluator that can be used in <a href=\"evaluate.html\">Evaluate</a> operation")
        ))
    }
    "## Output\n\n" + outputTable
  }

  /**
    * @param data Sequence of tuples (typeQualifier, description)
    */
  private def inputOutputTable(data: Seq[(String, String)]): String = {
    """
      |<table>
      |<thead>
      |<tr>
      |<th style="width:15%">Port</th>
      |<th style="width:15%">Type Qualifier</th>
      |<th style="width:70%">Description</th>
      |</tr>
      |</thead>
      |<tbody>
    """.stripMargin + tableRows(data) +
    """
      |</tbody>
      |</table>
      |""".stripMargin
  }

  private def tableRows(data: Seq[(String, String)]): String = {
    data.zipWithIndex.map(_ match {
      case ((typeQualifier, description), index) =>
        s"<tr><td><code>$index</code></td><td>$typeQualifier</td><td>$description</td></tr>"
    }).reduce((s1, s2) => s1 + s2)
  }

  private def parameters(operation: DOperation): String = {
    "## Parameters\n\n" + parametersTable(operation)
  }

  private def parametersTable(operation: DOperation): String = {
    """
      |<table class="table">
      |<thead>
      |<tr>
      |<th style="width:15%">Name</th>
      |<th style="width:15%">Type</th>
      |<th style="width:70%">Description</th>
      |</tr>
      |</thead>
      |<tbody>
      |""".stripMargin + extractParameters(operation) +
    """
      |</tbody>
      |</table>
      |""".stripMargin
  }

  private def extractParameters(operation: DOperation): String = {
    operation.params.map(param =>
      ParameterDescription(
        param.name,
        sparkParamType(param),
        DocUtils.forceDotAtEnd(param.description) + extraDescription(param)))
      .map(paramDescription => parameterTableEntry(paramDescription))
      .reduce((s1, s2) => s1 + s2)
  }

  private def sparkParamType(param: Param[_]): String = {
    param match {
      case (p: BooleanParam) => "Boolean"
      case (p: ChoiceParam[_]) => "SingleChoice"
      case (p: ColumnSelectorParam) => "MultipleColumnSelector"
      case (p: NumericParam) => "Numeric"
      case (p: MultipleChoiceParam[_]) => "MultipleChoice"
      case (p: PrefixBasedColumnCreatorParam) => "String"
      case (p: SingleColumnCreatorParam) => "String"
      case (p: SingleColumnSelectorParam) => "SingleColumnSelector"
      case (p: StringParam) => "String"
      case _ => throw new RuntimeException(
        "Unexpected parameter of class " + param.getClass.getSimpleName)
    }
  }

  private def parameterTableEntry(paramDescription: ParameterDescription): String = {
    val anchor = paramTypeAnchor(paramDescription.paramType)
    s"""
      |<tr>
      |<td><code>${paramDescription.name}</code></td>
      |<td><code><a href="../parameters.html#$anchor">${paramDescription.paramType}</a></code></td>
      |<td>${paramDescription.description}</td>
      |</tr>
      |""".stripMargin
  }

  private def paramTypeAnchor(paramType: String) = {
    paramType.replaceAll("(.)([A-Z])", "$1_$2").toLowerCase
  }

  private def extraDescription(param: Param[_]): String = {
    param match {
      case (p: AbstractChoiceParam[_, _]) => " Possible values: " + choiceValues(p.choiceInstances)
      case _ => ""
    }
  }

  private def choiceValues(choices: Seq[Choice]): String =
    "<code>[" + choices.map("\"" + _.name + "\"").mkString(", ") + "]</code>"

  private case class ParameterDescription(
    name: String,
    paramType: String,
    description: String)
}
