/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation, DOperation0To1, ExecutionContext}

/**
 * Operation which is able to read File from HDFS.
 * Current version only supports CSV
 */
class ReadFile extends DOperation0To1[File] {
  override val id: DOperation.Id = "981fc327-c755-4b77-8ae1-899f75811814"

  override val parameters = ParametersSchema(
    ReadFile.pathParam -> StringParameter(
      "HDFS path to file", None, required = true, validator = new AcceptAllRegexValidator),
    ReadFile.lineSeparatorParam -> StringParameter(
      "Line separator", Some("\n"), required = true, validator = new AcceptAllRegexValidator)
  )

  override val name: String = "Read File"

  override protected def _execute(context: ExecutionContext)(): File = {
    ???
  }
}

object ReadFile {
  val pathParam = "path"
  val lineSeparatorParam = "lineSeparator"
}
