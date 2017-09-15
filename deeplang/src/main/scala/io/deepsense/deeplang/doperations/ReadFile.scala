/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.doperations

import java.io.FileNotFoundException

import scala.collection.immutable.ListMap

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.protocol.HdfsFileStatus
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.doperables.file.File
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ChoiceParameter, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation, DOperation0To1, ExecutionContext}

/**
 * Operation which is able to read File from HDFS.
 * Current version only supports CSV
 */

case class ReadFile() extends DOperation0To1[File] {

  override val id: DOperation.Id = "748975b2-38f0-40b4-8550-3faf4840b7c5"

  override val parameters = ParametersSchema(
    ReadFile.pathParam -> StringParameter(
      "HDFS path to file", None, true, new AcceptAllRegexValidator),
    ReadFile.lineSeparatorParam ->
      ChoiceParameter("Line separator", Some(ReadFile.unixSeparatorLabel), true, ListMap(
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
    val fileInfo = Option(context.hdfsClient.hdfsClient.getFileInfo(path)) match {
      case Some(hdfsFileInfo) => hdfsFileInfo
      case None => throw new FileNotFoundException(path)
    }
    File(Some(lines), Some(ReadFile.buildReportMap(fileInfo)))
  }

  def chosenLineSeparatorValue: String = {
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

  /**
   * Creates ReadFile operation with parameters already set.
   * @param path A path of a file to read.
   * @param separator A line separator used in the file.
   * @return ReadFile operation.
   */
  def apply(path: String, separator: String): ReadFile = {
    val readFile = new ReadFile
    val params = readFile.parameters
    params.getStringParameter(pathParam).value = Some(path)
    val separatorChoice: ChoiceParameter = params.getChoiceParameter(lineSeparatorParam)
    separatorChoice.value = Some(customLineSeparatorLabel)
    separatorChoice.options(customLineSeparatorLabel)
      .getStringParameter(customLineSeparatorParam).value = Some(separator)
    readFile
  }
}
