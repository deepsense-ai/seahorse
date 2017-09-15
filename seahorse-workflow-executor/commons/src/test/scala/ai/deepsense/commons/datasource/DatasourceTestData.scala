/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.commons.datasource

import java.util.UUID

import org.joda.time.DateTime

import ai.deepsense.api.datasourcemanager.model.{AccessLevel, DatasourceParams, DatasourceType, Visibility, _}

object DatasourceTestData {
  def multicharSeparatorLibraryCsvDatasource: Datasource = {
    val ds = new Datasource
    val libraryFileParams = new LibraryFileParams
    libraryFileParams.setLibraryPath("some_path")
    libraryFileParams.setFileFormat(FileFormat.CSV)
    val csvType = new CsvFileFormatParams
    csvType.setConvert01ToBoolean(false)
    csvType.setIncludeHeader(false)
    csvType.setSeparatorType(CsvSeparatorType.CUSTOM)
    csvType.setCustomSeparator(",,")
    libraryFileParams.setCsvFileFormatParams(csvType)

    val params = new DatasourceParams
    params.setDatasourceType(DatasourceType.LIBRARYFILE)
    params.setLibraryFileParams(libraryFileParams)
    params.setName("name")
    params.setVisibility(Visibility.PUBLICVISIBILITY)

    ds.setCreationDateTime(new DateTime)
    ds.setParams(params)
    ds.setId(UUID.randomUUID.toString)
    ds.setCreationDateTime(new DateTime())
    ds.setAccessLevel(AccessLevel.WRITEREAD)
    ds.setOwnerId("abcd")
    ds.setOwnerName("owner_name")
    ds
  }
}
