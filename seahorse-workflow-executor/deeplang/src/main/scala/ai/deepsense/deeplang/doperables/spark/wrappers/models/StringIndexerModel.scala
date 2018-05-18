/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables.spark.wrappers.models

import org.apache.spark.ml.feature.{StringIndexer => SparkStringIndexer, StringIndexerModel => SparkStringIndexerModel}
import spray.json.{JsObject, JsString}

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.report.CommonTablesGenerators.SparkSummaryEntry
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.doperables.serialization.{JsonObjectPersistence, PathsUtils, SerializableSparkModel}
import ai.deepsense.deeplang.doperables.{MultiColumnModel, SparkSingleColumnModelWrapper, Transformer}
import ai.deepsense.deeplang.params.Param

trait StringIndexerModel extends Transformer

case class MultiColumnStringIndexerModel()
  extends MultiColumnModel[
    SparkStringIndexerModel,
    SparkStringIndexer,
    SingleColumnStringIndexerModel]
  with StringIndexerModel {

  override def getSpecificParams: Array[Param[_]] = Array()

  override def report(extended: Boolean = true): Report = {
    val tables = models.map(model => model.report(extended).content.tables)
    val name = s"${this.getClass.getSimpleName} with ${models.length} columns"
    tables
      .foldRight (super.report(extended).withReportName(name))(
        (seqTables, accReport) =>
          seqTables.foldRight(accReport)((t, r) => r.withAdditionalTable(t)))
  }

  override def loadTransformer(ctx: ExecutionContext, path: String): this.type = {
    this.setModels(loadModels(ctx, path))
  }

  override protected def saveTransformer(ctx: ExecutionContext, path: String): Unit = {
    saveModels(ctx, path)
  }

  private def saveModels(ctx: ExecutionContext, path: String): Unit = {
    saveNumberOfModels(ctx, path, models.size)
    val modelsPath = Transformer.modelFilePath(path)
    models.zipWithIndex.foreach { case (model, index) =>
      val modelPath = PathsUtils.combinePaths(modelsPath, index.toString)
      model.save(ctx, modelPath)
    }
  }

  private def saveNumberOfModels(ctx: ExecutionContext, path: String, numberOfModels: Int): Unit = {
    val json = JsObject(
      MultiColumnStringIndexerModel.numberOfModelsKey -> JsString(numberOfModels.toString)
    )
    val numberOfModelsFilePath = MultiColumnStringIndexerModel.numberOfModelsPath(path)
    JsonObjectPersistence.saveJsonToFile(ctx, numberOfModelsFilePath, json)
  }

  private def loadModels(
      ctx: ExecutionContext,
      path: String): Seq[SingleColumnStringIndexerModel] = {
    val numberOfModels = loadNumberOfModels(ctx, path)
    val modelsPath = Transformer.modelFilePath(path)
    (0 until numberOfModels).map { index =>
      val modelPath = PathsUtils.combinePaths(modelsPath, index.toString)
      Transformer.load(ctx, modelPath).asInstanceOf[SingleColumnStringIndexerModel]
    }
  }

  private def loadNumberOfModels(ctx: ExecutionContext, path: String): Int = {
    val numberOfModelsFilePath = MultiColumnStringIndexerModel.numberOfModelsPath(path)
    val json = JsonObjectPersistence.loadJsonFromFile(ctx, numberOfModelsFilePath).asJsObject
    json.fields(MultiColumnStringIndexerModel.numberOfModelsKey).convertTo[String].toInt
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkStringIndexerModel] = {
    throw new UnsupportedOperationException(
      "There is no single model to load for MultiColumnStringIndexerModel")
  }
}

object MultiColumnStringIndexerModel {
  val numberOfModelsKey = "numberOfModels"
  val numberOfModelsFileName = "numberOfModels"

  def numberOfModelsPath(path: String): String = {
    PathsUtils.combinePaths(path, numberOfModelsFileName)
  }
}

class SingleColumnStringIndexerModel
  extends SparkSingleColumnModelWrapper[SparkStringIndexerModel, SparkStringIndexer]
  with StringIndexerModel {

  override def getSpecificParams: Array[Param[_]] = Array()

  override def report(extended: Boolean = true): Report = {
    val summary =
      List(
        SparkSummaryEntry(
          name = "labels",
          value = sparkModel.labels,
          description = "Ordered list of labels, corresponding to indices to be assigned."))

    super.report(extended)
      .withAdditionalTable(CommonTablesGenerators.modelSummary(summary))
  }

  override protected def loadModel(
      ctx: ExecutionContext,
      path: String): SerializableSparkModel[SparkStringIndexerModel] = {
    new SerializableSparkModel(SparkStringIndexerModel.load(path))
  }
}

