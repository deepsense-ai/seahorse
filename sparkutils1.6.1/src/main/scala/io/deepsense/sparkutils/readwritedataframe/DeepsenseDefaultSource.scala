/**
 * Copyright 2016, deepsense.io
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

// This package is needed because of Spark's package access modifiers
package com.databricks.spark.csv

import com.databricks.spark.csv.util.{ParserLibs, TypeCast}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.sources._

/**
  * Lambdas are serializable depending on whether parent object is serializable.
  *
  * To not rely on this hidden assumption explicit SerializableConstantFunction is introduced.
  */
case class SerializableConstantFunction[T <: Serializable](value: T)
  extends (() => T) with Serializable {
  override def apply(): T = value
}

/**
  * This is copy-pasta of Spark's com.databricks.spark.csv.DefaultSource
  *
  * In our case we need to pass arbitrary rdd in here.
  * But Spark's implementation is hardcoded against file.
  *
  * DefaultSource.createRelation returns CsvRelation. Unfortunately
  * file path checking on all nodes occurs at the time of `createRelation` call,
  * so it's not possible to plug our arbitrary RDD in csvRelation returned from `createRelation`.
  *
  * This version removes all references to 'path' variables, gets rid of all
  * file datasources and uses arbitrary rdd passed in here in constructor.
  */
object DeepsenseDefaultSource {

  def createRelation(
      sqlContext: SQLContext,
      parameters: Map[String, String],
      baseRDD: RDD[String]): BaseRelation = {
    val delimiter = TypeCast.toChar(parameters.getOrElse("delimiter", ","))

    val quote = parameters.getOrElse("quote", "\"")
    val quoteChar: Character = if (quote == null) {
      null
    } else if (quote.length == 1) {
      quote.charAt(0)
    } else {
      throw new Exception("Quotation cannot be more than one character.")
    }

    val escape = parameters.getOrElse("escape", null)
    val escapeChar: Character = if (escape == null) {
      null
    } else if (escape.length == 1) {
      escape.charAt(0)
    } else {
      throw new Exception("Escape character cannot be more than one character.")
    }

    val comment = parameters.getOrElse("comment", "#")
    val commentChar: Character = if (comment == null) {
      null
    } else if (comment.length == 1) {
      comment.charAt(0)
    } else {
      throw new Exception("Comment marker cannot be more than one character.")
    }

    val parseMode = parameters.getOrElse("mode", "PERMISSIVE")

    val useHeader = parameters.getOrElse("header", "false")
    val headerFlag = if (useHeader == "true") {
      true
    } else if (useHeader == "false") {
      false
    } else {
      throw new Exception("Header flag can be true or false")
    }

    val parserLib = parameters.getOrElse("parserLib", ParserLibs.DEFAULT)
    val ignoreLeadingWhiteSpace = parameters.getOrElse("ignoreLeadingWhiteSpace", "false")
    val ignoreLeadingWhiteSpaceFlag = if (ignoreLeadingWhiteSpace == "false") {
      false
    } else if (ignoreLeadingWhiteSpace == "true") {
      if (!ParserLibs.isUnivocityLib(parserLib)) {
        throw new Exception("Ignore whitesspace supported for Univocity parser only")
      }
      true
    } else {
      throw new Exception("Ignore white space flag can be true or false")
    }
    val ignoreTrailingWhiteSpace = parameters.getOrElse("ignoreTrailingWhiteSpace", "false")
    val ignoreTrailingWhiteSpaceFlag = if (ignoreTrailingWhiteSpace == "false") {
      false
    } else if (ignoreTrailingWhiteSpace == "true") {
      if (!ParserLibs.isUnivocityLib(parserLib)) {
        throw new Exception("Ignore whitespace supported for the Univocity parser only")
      }
      true
    } else {
      throw new Exception("Ignore white space flag can be true or false")
    }
    val treatEmptyValuesAsNulls = parameters.getOrElse("treatEmptyValuesAsNulls", "false")
    val treatEmptyValuesAsNullsFlag = if (treatEmptyValuesAsNulls == "false") {
      false
    } else if (treatEmptyValuesAsNulls == "true") {
      true
    } else {
      throw new Exception("Treat empty values as null flag can be true or false")
    }

    val inferSchema = parameters.getOrElse("inferSchema", "false")
    val inferSchemaFlag = if (inferSchema == "false") {
      false
    } else if (inferSchema == "true") {
      true
    } else {
      throw new Exception("Infer schema flag can be true or false")
    }
    val nullValue = parameters.getOrElse("nullValue", "")

    val dateFormat = parameters.getOrElse("dateFormat", null)

    val codec = parameters.getOrElse("codec", null)

    CsvRelation(
      SerializableConstantFunction(baseRDD),
      None,
      headerFlag,
      delimiter,
      quoteChar,
      escapeChar,
      commentChar,
      parseMode,
      parserLib,
      ignoreLeadingWhiteSpaceFlag,
      ignoreTrailingWhiteSpaceFlag,
      treatEmptyValuesAsNullsFlag,
      userSchema = null,
      inferSchemaFlag,
      codec,
      nullValue,
      dateFormat)(sqlContext)
  }
}
