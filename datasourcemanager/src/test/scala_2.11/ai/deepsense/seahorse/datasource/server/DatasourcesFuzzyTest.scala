/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.seahorse.datasource.server

import java.util.UUID

import scala.util.{Failure, Success, Try}

import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck.Test.Parameters
import org.scalacheck.{Arbitrary, Gen, Properties}

import ai.deepsense.commons.utils.LoggerForCallerClass
import ai.deepsense.seahorse.datasource.api.ApiException
import ai.deepsense.seahorse.datasource.converters.DatasourceApiFromDb
import ai.deepsense.seahorse.datasource.model.DatasourceType.{apply => _}
import ai.deepsense.seahorse.datasource.model._

/**
 * This test is for Scala 2.11 only
 * since Arbitrary instances aren't generated in 2.10 for case classes.
 */
class DatasourcesFuzzyTest extends Properties("DatasourcesApi") {

  ///////////////////////////////////////////////////
  // IF THIS FAILS BECAUSE OF 'implicit not found' //
  // you need to add generator for enum below      //
  ///////////////////////////////////////////////////

  private val logger = LoggerForCallerClass()

  // Scalatest 3.0.0 is now compatible with scalacheck 1.13
  // (which is needed because scalacheck-shapeless depends on 1.13)
  // Once Scalatest is compatible change it to normal Scalatest Suite

  private implicit lazy val api = ApiForTests.api

  implicit def enumArb[E <: Enumeration](enum: E): Arbitrary[E#Value] = Arbitrary(
    Gen.oneOf(enum.values.toList.map(_.asInstanceOf[E#Value]))
  )

  // TODO Find generic way to generate arbitrary instances for enums
  // Probably rework enumArb method so it does not need instance argument
  // (it should only need type argument E)

  implicit val accessLevelArb = enumArb(AccessLevel)
  implicit val datasourceTypeArb = enumArb(DatasourceType)
  implicit val fileFormatArb = enumArb(FileFormat)
  implicit val visibilityArb = enumArb(Visibility)
  implicit val csvSeparatorTypeArb = enumArb(CsvSeparatorType)

  property("Datasources Manager should never throw unexpected exception") = {
    forAll { (ds: DatasourceParams) =>
      Try {
        val userId = UUID.randomUUID()
        val datasourceId = UUID.randomUUID()
        api.putDatasourceImpl(userId, "SomeUserName", datasourceId, ds)
        val dsWithoutIgnoredFields = api.getDatasourceImpl(userId, datasourceId).params

        val result = compareIgnoringExtraOptionalsFromLeftArgument(ds, dsWithoutIgnoredFields)

        if (!result) {
          logger.error(s"$ds does not equal (ignoring extra options) $dsWithoutIgnoredFields")
        }

        result
      } match {
        case Success(result) => result
        case Failure(ex: ApiException) if ex.errorCode != unexpectedExceptionCode => true
        case Failure(otherEx) =>
          otherEx.printStackTrace()
          false
      }
    }
  }

  // If there are extra unnecessary fields defined (like JDBC params for EXTERNAL_FILE)
  // they are ignored by backend. This comparison account for that.
  private def compareIgnoringExtraOptionalsFromLeftArgument(left: Product, right: Product): Boolean = {
    val allCompareResults = for {
      entries <- left.productIterator zip right.productIterator
    } yield {
      entries match {
        case (recProduct1: Product, recProduct2: Product) =>
          compareIgnoringExtraOptionalsFromLeftArgument(recProduct1, recProduct2)
        case (Some(_), None) => ignoreExtraOptional
        case (v1, v2) => v1 == v2
      }
    }
    val atLeastOneComparisonFailed = allCompareResults.contains(false)
    !atLeastOneComparisonFailed
  }

  private val ignoreExtraOptional = true
  private val unexpectedExceptionCode = 501

}
