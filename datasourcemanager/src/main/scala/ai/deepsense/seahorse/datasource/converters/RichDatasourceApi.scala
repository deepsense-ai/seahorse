/**
 * Copyright 2018 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.seahorse.datasource.converters

import scalaz.Validation
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import ai.deepsense.commons.service.api.CommonApiExceptions.ApiException
import ai.deepsense.seahorse.datasource.model.FileFormat.FileFormat
import ai.deepsense.seahorse.datasource.model._


case class FileFormatParam(
  fileFormat : FileFormat,
  csvFileFormatParams : Option[CsvFileFormatParams],
  sparkGenericFileFormatParams: Option[SparkGenericFileFormatParams])

object RichDatasourceParams {

  def getSparkFormat(fileFormatParam: FileFormatParam):
    Validation[ApiException, Option[SparkGenericFileFormatParams]] = {
    fileFormatParam.fileFormat match {
      case FileFormat.sparkgeneric => for {
        sparkGeneric <- validateDefined("sparkGenericFileFormatParams", fileFormatParam.sparkGenericFileFormatParams)
      } yield Some(sparkGeneric)
      case _ => None.success
    }
  }

  implicit class RichDatasourceParams(self: DatasourceParams) {

    def getFileParams(): Validation[ApiException, Option[FileFormatParam]] = {
      self.datasourceType match {
        case DatasourceType.jdbc => None.success
        case DatasourceType.googleSpreadsheet => None.success
        case DatasourceType.hdfs => for {
          param <- validateDefined("hdfsParams", self.hdfsParams)
        } yield Some(FileFormatParam(param.fileFormat, param.csvFileFormatParams, param.sparkGenericFileFormatParams))
        case DatasourceType.externalFile => for {
          param <- validateDefined("externalFileParams", self.externalFileParams)
        } yield Option(FileFormatParam(param.fileFormat, param.csvFileFormatParams, param.sparkGenericFileFormatParams))
        case DatasourceType.libraryFile => for {
          param <- validateDefined("libraryFileParams", self.libraryFileParams)
        } yield Option(FileFormatParam(param.fileFormat, param.csvFileFormatParams, param.sparkGenericFileFormatParams))
      }
    }

    def getSparkOptions(): Validation[ApiException, List[SparkGenericOptions]] = {
      for {
        fileParamsOpt <- getFileParams()
        sparkFormatOpt <- validationIdentity(fileParamsOpt, getSparkFormat _)
      } yield {
        sparkFormatOpt match {
          case Some(sparkFormat) => sparkFormat.sparkOptions
          case None => List()
        }
      }
    }

  }
}
