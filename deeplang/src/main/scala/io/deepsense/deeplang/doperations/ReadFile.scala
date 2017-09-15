/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.doperations

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.protocol.HdfsFileStatus
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat

import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.parameters.{ChoiceParameter, AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation, DOperation0To1, ExecutionContext}
import io.deepsense.commons.datetime.DateTimeConverter

/**
 * Operation which is able to read File from HDFS.
 * Current version only supports CSV
 */

class ReadFile extends DOperation0To1[File] {

  override val id: DOperation.Id = "748975b2-38f0-40b4-8550-3faf4840b7c5"

  override val parameters = ParametersSchema(
    ReadFile.pathParam -> StringParameter(
      "HDFS path to file", None, true, new AcceptAllRegexValidator),
    ReadFile.lineSeparatorParam ->
      ChoiceParameter("Line separator", Some(ReadFile.unixSeparatorLabel), true, Map(
        ReadFile.unixSeparatorLabel -> ParametersSchema(),
        ReadFile.windowsSeparatorLabel -> ParametersSchema(),
        ReadFile.customLineSeparatorLabel -> ParametersSchema(ReadFile.customLineSeparatorParam ->
          StringParameter(
            "Custom line separator", None, true, new AcceptAllRegexValidator())))))

  override val name: String = "Read File"

  override protected def _execute(context: ExecutionContext)(): File = {
    val path = parameters.getString(ReadFile.pathParam).get
    val separator = chosenLineSeparatorValue
    val sparkContext = context.sqlContext.sparkContext

    val conf = new Configuration(sparkContext.hadoopConfiguration)
    conf.set(ReadFile.recordDelimiterSettingName, separator)

    val lines = context.sqlContext.sparkContext.newAPIHadoopFile(
      path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text], conf)
      .map { case (_, text) => text.toString }
    val fileInfo = context.hdfsClient.hdfsClient.getFileInfo(path)
    File(Some(lines), Some(ReadFile.buildReportMap(fileInfo)))
  }

  def chosenLineSeparatorValue(): String = {
    val chosenParam = parameters.getChoice(ReadFile.lineSeparatorParam).get
    chosenParam.label match {
      case ReadFile.windowsSeparatorLabel => ReadFile.windowsSeparatorValue
      case ReadFile.unixSeparatorLabel => ReadFile.unixSeparatorValue
      case ReadFile.customLineSeparatorLabel =>
        chosenParam
          .selectedSchema
          .getString(ReadFile.customLineSeparatorParam)
          .get
      case _ => ReadFile.defaultSeparatorValue
    }
  }
}

object ReadFile {

  val recordDelimiterSettingName = "textinputformat.record.delimiter"

  val pathParam = "path"
  val lineSeparatorParam = "lineSeparator"
  val customLineSeparatorParam = "customLineSeparator"

  val windowsSeparatorLabel = "Windows line separator"
  val unixSeparatorLabel = "Unix line separator"
  val customLineSeparatorLabel = "Custom line separator"

  val windowsSeparatorValue = "\r\n"
  val unixSeparatorValue = "\n"
  val defaultSeparatorValue = unixSeparatorLabel

  def buildReportMap(fileStatus : HdfsFileStatus) : Map[String, String] = {
    val modificationDateTime = DateTimeConverter.fromMillis(fileStatus.getModificationTime)
    val modificationStr = DateTimeConverter.toString(modificationDateTime)
    Map(
      "Size" -> fileStatus.getLen.toString,
      "Modification time" -> modificationStr)
  }
}
