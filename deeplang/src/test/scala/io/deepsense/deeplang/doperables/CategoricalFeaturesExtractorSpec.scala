/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.deeplang.doperables

import org.mockito.Mockito

import io.deepsense.deeplang.UnitSpec
import io.deepsense.deeplang.doperables.dataframe._
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping
import io.deepsense.deeplang.parameters.ColumnType

class CategoricalFeaturesExtractorSpec extends UnitSpec {

  "CategoricalFeaturesExtractor" should {
    "extract empty categorical mappings" when {
      "metadata is missing" in {
        verifyResult(None, Seq.empty, Map.empty)
      }

      "metadata is empty" in {
        verifyResult(Some(DataFrameMetadata.empty), Seq.empty, Map.empty)
      }

      "metadata has no categorical columns" in {
        verifyResult(createMetadata(
          CommonColumnMetadata("col0", Some(0), Some(ColumnType.boolean)),
          CommonColumnMetadata("col1", Some(1), Some(ColumnType.numeric)),
          CommonColumnMetadata("col2", Some(2), Some(ColumnType.string)),
          CommonColumnMetadata("col3", Some(3), Some(ColumnType.timestamp))
        ), Seq("col0", "col1", "col2", "col3"), Map.empty)
      }

      "categorical column has no categories" in {
        verifyResult(createMetadata(
          CategoricalColumnMetadata("col0", Some(0), None)
        ), Seq("col0"), Map.empty)
      }

      "no categorical column is selected" in {
        verifyResult(createMetadata(
          CategoricalColumnMetadata("col0", Some(0), Some(CategoriesMapping(Seq("cat1", "cat2"))))
        ), Seq.empty, Map.empty)
      }
    }

    "extract mappings for categorical columns" when {
      "metadata has categorical column" in {
        verifyResult(createMetadata(
          CategoricalColumnMetadata("col0", Some(0), Some(CategoriesMapping(Seq("cat1", "cat2"))))
        ), Seq("col0"), Map(0 -> 2))
      }

      "metadata has multiple columns" in {
        verifyResult(createMetadata(
          CategoricalColumnMetadata("col0", Some(0), Some(CategoriesMapping(Seq("cat1", "cat2")))),
          CommonColumnMetadata("col1", Some(1), Some(ColumnType.boolean)),
          CategoricalColumnMetadata("col2", Some(2), Some(CategoriesMapping(Seq("cat1")))),
          CommonColumnMetadata("col3", Some(3), Some(ColumnType.numeric)),
          CategoricalColumnMetadata("col4", Some(4), Some(CategoriesMapping(Seq.empty))),
          CategoricalColumnMetadata("no-index", None, Some(CategoriesMapping(Seq.empty))),
          CategoricalColumnMetadata("no-categories", Some(100), None)
        ),
          Seq("col0", "col1", "col2", "col3", "col4", "no-index", "no-categories"),
          Map(0 -> 2, 2 -> 1))
      }

      "only some features are selected" in {
        verifyResult(createMetadata(
          CategoricalColumnMetadata("col0", Some(0), Some(CategoriesMapping(Seq("cat1", "cat2")))),
          CommonColumnMetadata("col1", Some(1), Some(ColumnType.boolean)),
          CategoricalColumnMetadata("col2", Some(2), Some(CategoriesMapping(Seq("cat1")))),
          CommonColumnMetadata("col3", Some(3), Some(ColumnType.numeric)),
          CategoricalColumnMetadata("col4", Some(4), Some(CategoriesMapping(Seq.empty))),
          CategoricalColumnMetadata("no-index", None, Some(CategoriesMapping(Seq.empty))),
          CategoricalColumnMetadata("no-categories", Some(100), None)
        ),
          Seq("col2", "col0", "col4", "no-categories"),
          Map(0 -> 1, 1 -> 2))
      }
    }
  }

  private def verifyResult(
      metadata: Option[DataFrameMetadata],
      featureColumns: Seq[String],
      expectedResult: Map[Int, Int]): Unit = {
    val extractor = new CategoricalFeaturesExtractor {}
    val dataframe = mock[DataFrame]
    Mockito.when(dataframe.metadata).thenReturn(metadata)
    val extractedMappings = extractor.extractCategoricalFeatures(dataframe, featureColumns)
    extractedMappings should contain theSameElementsAs expectedResult
  }

  private def createMetadata(columns: ColumnMetadata*): Option[DataFrameMetadata] = {
    Some(DataFrameMetadata(false, false, columns.map(c => c.name -> c).toMap))
  }
}
